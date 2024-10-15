def call(Map pipelineParams) {
  def label = "jenkin-slave${UUID.randomUUID().toString()}"
  cluster = 'ovh'
  JENKINS_URL = "${JENKINS_URL}"
  jenkinUrlIndexOf = JENKINS_URL.indexOf('local')

  if (jenkinUrlIndexOf > -1) {
    cluster = 'local'
  }

  cloud = "kubernetes-${cluster}"
  serviceAccount = "sa-jenkins-${cluster}"

  podTemplate(
    label: label,
    cloud: cloud,
    serviceAccount: serviceAccount,
    containers: [
      containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:4.13.3-1-jdk11', args: '${computer.jnlpmac} ${computer.name}'),
      containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
      containerTemplate(name: 'helm', image: 'alpine/helm:3.11.3', command: 'cat', ttyEnabled: true)
    ],
    volumes: [
      hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
    ]
  )
  {
    node(label){
      def appName = "${pipelineParams.app_name}"
      def myDeploy
      String srcGitCredentials = "jenkins_gitlab"
      String srcRepoUrl = "${pipelineParams.src_repo_url}"
      def releaseName = ''
      def environment = ''
      def dockerImagerepository = ''
      def dockerImageTag = ''
      def k8sUrl = 'https://115.79.204.120:6443' //url of k8s-local
      stage('Checkout & preparation') {
        //myRepo = checkout scm
        myDeploy = checkout scm // clone deploy
        sh 'ls ./'
        echo "check myDeploy ${myDeploy}"
        
        dir('RepoProject') {
          obj = checkoutAndPreparation(
            srcGitCredentials:srcGitCredentials,
            srcRepoUrl:srcRepoUrl,
            appName:appName
          )
          releaseName = obj.releaseName
          environment = obj.environment
          dockerImagerepository = obj.dockerImagerepository
          dockerImageTag = obj.dockerImageTag
        }
      }

      stage('Build docker image') {
        dir('RepoProject') {
          sh 'ls ./'
          //add folder ignore into .dockerignore
          sh "echo ./.git >> ./.dockerignore"
          container('docker') {
            buildAndPushDockerImage(
              dockerImagerepository:dockerImagerepository,
              dockerImageTag:dockerImageTag,
              environment:environment
            )
          }
        }
      }

      stage('Unit & integration test') {
        container('helm') {
          unitIntegrationTest()
        }
      }

      stage("Deploy ${environment}") {
        container('helm') {
          deployToKubernetes(
            releaseName:releaseName,
            appName:appName,
            environment:environment,
            dockerImagerepository:dockerImagerepository,
            dockerImageTag:dockerImageTag,
            cluster:cluster,
            k8sUrl:k8sUrl
          )
        }
      }
    }
  }
}