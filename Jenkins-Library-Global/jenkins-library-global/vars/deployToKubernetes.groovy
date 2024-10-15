def call(Map args) {
  echo "${args.dockerImagerepository} upgrading . . ."
  withCredentials([usernamePassword(credentialsId: 'jenkins_registry', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
    // install helm common
    sh("helm registry login docker.io -u ${USERNAME} -p ${PASSWORD}")
    sh("helm dependency update ./${args.appName}")
    // helm deploy to k8s
    sh("helm upgrade --install ${args.releaseName} ./${args.appName} \
    --namespace=rnd-${args.environment} \
    --set helm-common.environment=${args.environment} \
    --set helm-common.image.repository=${args.dockerImagerepository} \
    --set helm-common.image.tag=${args.dockerImageTag} \
    --values ./${args.appName}/values.${args.environment}.yaml \
    --set helm-common.cluster=${args.cluster} \
    --reuse-values \
    --history-max 3 ")
  }
  println("Deploy to ${args.environment} success!!")
}