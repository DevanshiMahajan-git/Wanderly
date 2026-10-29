pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Source code checked out from GitHub.'
            }
        }

        stage('Build and Test') {
            steps {
                bat '"C:\\Users\\Prakash\\Downloads\\apache-maven-3.10.0-bin\\apache-maven-3.10.0\\bin\\mvn.cmd" -B clean verify'
            }
        }
    }

    post {
        success {
            echo 'Wanderly pipeline passed.'
        }
        failure {
            echo 'Review the stage logs for errors.'
        }
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
        }
    }
}
