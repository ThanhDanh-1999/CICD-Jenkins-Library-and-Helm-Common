def call(Map args) {
  def myRepo = ''
  def gitBranchName = ''
  def shortGitCommit = ''
  def dockerImageName = ''
  def dockerImageTag = ''
  def environment = ''
  def releaseName = ''
  def dockerImagerepository = ''
  echo "Working on  params.Source_Code_Branch ${params.Source_Code_Branch} ..."
  echo "Working on Source_Code_Branch ${Source_Code_Branch} ..."

  myRepo = checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: args.srcRepoUrl, credentialsId: args.srcGitCredentials]], branches: [[name: "${Source_Code_Branch}"]]],poll: false
  gitBranchName = env.gitlabTargetBranch? env.gitlabTargetBranch: myRepo.GIT_BRANCH;
  gitBranchName = gitBranchName.substring(gitBranchName.lastIndexOf('/')+1, gitBranchName.length())
    
  echo "Working on myRepo ${myRepo} ..."
  echo "Working on gitBranchName ${gitBranchName} ..."

  shortGitCommit = "${args.appName}-${myRepo.GIT_COMMIT[0..7]}"
  if((env.gitlabTargetBranch =~ /.+\/tags\/[0-9\.\-]+/) || (myRepo.GIT_BRANCH =~ /.+\/tags\/[0-9\.\-]+/)){
    dockerImageName = args.appName
    dockerImageTag = gitBranchName
    environment = 'production'
    releaseName = args.appName
    echo "At Tags"
  } else if(gitBranchName=='master'){
    dockerImageName = "${args.appName}-staging"
    dockerImageTag = shortGitCommit
    environment = 'staging'
    releaseName = "${args.appName}-staging"
    echo "At MASTER"
  } else if(gitBranchName == 'development' ){
    dockerImageName = "${args.appName}-testing"
    dockerImageTag = shortGitCommit
    environment = 'testing'
    releaseName = "${args.appName}-testing"
    echo "At DEVELOPMENT"
  } else {
    currentBuild.result = 'ABORTED'
    error('Stopping early… Now at another branch')
  }
  dockerImagerepository = "danhnt/${dockerImageName}"
  echo "Working on branch ${myRepo.GIT_BRANCH} ...${env.gitlabTargetBranch}"
  return[
    releaseName:releaseName,
    environment:environment,
    dockerImagerepository:dockerImagerepository,
    dockerImageTag:dockerImageTag
  ]

}