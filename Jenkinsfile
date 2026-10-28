pipeline {
  agent any
  tools { jdk 'JDK17'; maven 'Maven3' }
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Build and Test') { steps { bat 'mvn -B clean verify' } post { always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' } } }
    stage('Build Docker Image') { steps { bat 'docker build -t wanderly:%BUILD_NUMBER% .' } }
  }
  post { success { echo 'Wanderly pipeline passed.' } failure { echo 'Review the stage logs before proceeding.' } }
}
