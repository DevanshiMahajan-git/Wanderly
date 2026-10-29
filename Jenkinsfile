pipeline {
    agent any

    parameters {
        string(name: 'APP_PORT', defaultValue: '8081', description: 'Port on which Wanderly will run')
    }

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

        stage('Package Application') {
            steps {
                bat '"C:\\Users\\Prakash\\Downloads\\apache-maven-3.10.0-bin\\apache-maven-3.10.0\\bin\\mvn.cmd" -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy Application') {
            steps {
                echo "Deployment configuration: application port ${params.APP_PORT}"
                echo 'Executable JAR packaged and archived successfully.'
            }
        }
    }

    post {
        success {
            echo 'Wanderly CI/CD pipeline passed.'
        }
        failure {
            echo 'Review the stage logs for errors.'
        }
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
        }
    }
}
