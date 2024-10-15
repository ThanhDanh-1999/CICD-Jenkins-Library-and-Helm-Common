def call(Map args) {
  echo "${args.dockerImagerepository} upgrading . . ."
  withCredentials([usernamePassword(credentialsId: 'jenkins_registry', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
    // install helm common
    sh("helm registry login docker.io -u ${USERNAME} -p ${PASSWORD}")
    sh("helm dependency update ./${args.appName}")
    // helm deploy to k8s
    def helmUpgradeCmd = "helm upgrade --install ${args.releaseName} ./${args.appName} \
      --namespace=rnd-${args.environment} \
      --set helm-common-team-rnd.environment=${args.environment} \
      --set helm-common-team-rnd.image.repository=${args.dockerImagerepository} \
      --set helm-common-team-rnd.image.tag=${args.dockerImageTag} \
      --values ./${args.appName}/values.${args.environment}.yaml \
      --set helm-common-team-rnd.cluster=${args.cluster} \
      --history-max 3"
    if (args.environment == 'testing') {
      withKubeConfig([credentialsId: 'sa-jenkins-local', serverUrl: args.k8sUrl]) {
        sh(helmUpgradeCmd)
      }
    } else {
      sh(helmUpgradeCmd)
    }
  }
  println("Deploy to ${args.environment} success!!")
}