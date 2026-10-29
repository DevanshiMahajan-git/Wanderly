pipeline {
    agent any

    parameters {
        string(
            name: 'APP_PORT',
            defaultValue: '8081',
            description: 'Port on which Wanderly will run'
        )
    }

    environment {
        MAVEN_CMD = 'C:\\Users\\Prakash\\Downloads\\apache-maven-3.10.0-bin\\apache-maven-3.10.0\\bin\\mvn.cmd'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Source code is managed through GitHub SCM.'
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

                archiveArtifacts(
                    artifacts: 'target/*.jar',
                    fingerprint: true
                )
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '''
                    set "DOCKER_CONFIG=C:\\Users\\Prakash\\.docker"
                    "C:\\Users\\Prakash\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" --context desktop-linux build -t wanderly:latest .
                '''
            }
        }

        stage('Deploy Application') {
            steps {
                powershell '''
                    $ErrorActionPreference = "Stop"

                    $port = [int]$env:APP_PORT
                    $workspace = $env:WORKSPACE
                    $jar = Join-Path $workspace "target\\wanderly.jar"
                    $log = Join-Path $workspace "wanderly-app.log"
                    $errorLog = Join-Path $workspace "wanderly-app-error.log"
                    $pidFile = Join-Path $workspace "wanderly-deployment.pid"

                    if ($port -lt 1024 -or $port -gt 65535) {
                        throw "APP_PORT must be between 1024 and 65535."
                    }

                    if (-not (Test-Path $jar)) {
                        throw "Application JAR not found: $jar"
                    }

                    $listener = Get-NetTCPConnection `
                        -LocalPort $port `
                        -State Listen `
                        -ErrorAction SilentlyContinue

                    if ($listener) {
                        throw "Port $port is already occupied. No process was terminated."
                    }

                    $javaCommand = Get-Command java.exe -ErrorAction Stop
                    $javaPath = $javaCommand.Source

                    Remove-Item $log, $errorLog -Force -ErrorAction SilentlyContinue

                    $appProcess = Start-Process `
                        -FilePath $javaPath `
                        -ArgumentList @(
                            "-jar",
                            "`"$jar`"",
                            "--server.port=$port"
                        ) `
                        -WorkingDirectory $workspace `
                        -RedirectStandardOutput $log `
                        -RedirectStandardError $errorLog `
                        -PassThru

                    Set-Content -Path $pidFile -Value $appProcess.Id

                    Write-Host "Started Wanderly with PID $($appProcess.Id)."
                    Write-Host "Application log: $log"

                    $healthy = $false

                    for ($i = 0; $i -lt 30; $i++) {
                        Start-Sleep -Seconds 2

                        $appProcess.Refresh()

                        if ($appProcess.HasExited) {
                            Write-Host "Application exited during startup."
                            Get-Content $log -ErrorAction SilentlyContinue
                            Get-Content $errorLog -ErrorAction SilentlyContinue
                            throw "Wanderly stopped during startup."
                        }

                        try {
                            $response = Invoke-WebRequest `
                                -Uri "http://localhost:$port/" `
                                -UseBasicParsing `
                                -TimeoutSec 3

                            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                                $healthy = $true
                                break
                            }
                        }
                        catch {
                            # Continue waiting while Spring Boot starts.
                        }
                    }

                    if (-not $healthy) {
                        Get-Content $log -ErrorAction SilentlyContinue
                        Get-Content $errorLog -ErrorAction SilentlyContinue
                        throw "Wanderly did not respond within 60 seconds."
                    }

                    Start-Sleep -Seconds 5
                    $appProcess.Refresh()

                    if ($appProcess.HasExited) {
                        Get-Content $log -ErrorAction SilentlyContinue
                        Get-Content $errorLog -ErrorAction SilentlyContinue
                        throw "Wanderly responded initially but exited shortly afterward."
                    }

                    $listener = Get-NetTCPConnection `
                        -LocalPort $port `
                        -State Listen `
                        -ErrorAction SilentlyContinue

                    if (-not $listener) {
                        throw "Wanderly process exists, but port $port is no longer listening."
                    }

                    Write-Host "Deployment health check passed."
                    Write-Host "Wanderly is responding on port $port."
                    Write-Host "Process ID: $($appProcess.Id)"
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                powershell '''
                    $port = [int]$env:APP_PORT

                    try {
                        $response = Invoke-WebRequest `
                            -Uri "http://localhost:$port/" `
                            -UseBasicParsing `
                            -TimeoutSec 10

                        Write-Host "HTTP status: $($response.StatusCode)"
                        Write-Host "Wanderly is accessible on port $port."
                    }
                    catch {
                        throw "Deployment verification failed: $($_.Exception.Message)"
                    }
                '''
            }
        }
    }

    post {
        success {
            echo 'Wanderly build, tests, packaging, deployment, and verification succeeded.'
        }

        failure {
            echo 'Pipeline failed. Check the stage logs and wanderly-app.log.'
        }

        always {
            junit(
                allowEmptyResults: true,
                testResults: '**/target/surefire-reports/TEST-*.xml'
            )
        }
    }
}