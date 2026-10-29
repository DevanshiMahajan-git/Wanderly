pipeline {
    agent any

    parameters {
        string(name: 'APP_PORT', defaultValue: '8081',
               description: 'Port for the Wanderly application')
    }

    environment {
        MAVEN_CMD = 'C:\\Users\\Prakash\\Downloads\\apache-maven-3.10.0-bin\\apache-maven-3.10.0\\bin\\mvn.cmd'
        APP_DIR = 'C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\Wanderly-CI'
        APP_JAR = 'C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\Wanderly-CI\\target\\wanderly.jar'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out Wanderly source code.'
            }
        }

        stage('Build and Test') {
            steps {
                bat '"%MAVEN_CMD%" -B clean verify'
            }
        }

        stage('Package Application') {
            steps {
                bat '"%MAVEN_CMD%" -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy Application') {
            steps {
                powershell '''
                    $port = [int]$env:APP_PORT

                    if ($port -lt 1024 -or $port -gt 65535) {
                        throw "APP_PORT must be between 1024 and 65535."
                    }

                    $jar = Join-Path $env:WORKSPACE "target\\wanderly.jar"

                    if (-not (Test-Path $jar)) {
                        throw "Application JAR not found: $jar"
                    }

                    $existing = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue

                    if ($existing) {
                        throw "Port $port is already in use. Stopping deployment to avoid terminating an unrelated process."
                    }

                    $process = Start-Process -FilePath "java" `
                        -ArgumentList @("-jar", "`"$jar`"", "--server.port=$port") `
                        -WorkingDirectory $env:WORKSPACE `
                        -PassThru

                    $healthy = $false

                    for ($i = 0; $i -lt 30; $i++) {
                        Start-Sleep -Seconds 2

                        if ($process.HasExited) {
                            throw "Wanderly exited during startup. Check the Jenkins workspace and application logs."
                        }

                        try {
                            $response = Invoke-WebRequest -Uri "http://localhost:$port/" -UseBasicParsing -TimeoutSec 3
                            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                                $healthy = $true
                                break
                            }
                        } catch {
                            # The application may still be starting.
                        }
                    }

                    if (-not $healthy) {
                        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
                        throw "Wanderly did not respond on port $port within 60 seconds."
                    }

                    Set-Content -Path (Join-Path $env:WORKSPACE "wanderly-deployment.pid") -Value $process.Id
                    Write-Host "Wanderly is running on port $port. PID: $($process.Id)"
                '''
            }
        }
    }

    post {
        success {
            echo 'Wanderly build, tests, packaging, and deployment completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Review the stage logs.'
        }

        always {
            junit allowEmptyResults: true,
                  testResults: '**/target/surefire-reports/TEST-*.xml'
        }
    }
}