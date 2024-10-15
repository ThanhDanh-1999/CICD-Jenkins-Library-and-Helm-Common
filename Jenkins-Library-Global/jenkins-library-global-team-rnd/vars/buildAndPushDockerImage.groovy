def call(Map args) {
  docker.withRegistry("", 'jenkins_registry') {
    echo "Bulding docker images ..."
    docker.build(
      "${args.dockerImagerepository}:${args.dockerImageTag}"
    )
    
    echo "Pushing the image to docker hub ..."
    def dockerImage = docker.image("${args.dockerImagerepository}:${args.dockerImageTag}")
    dockerImage.push()
  }
  echo "Push image ${args.dockerImagerepository} to registry successfull"
}