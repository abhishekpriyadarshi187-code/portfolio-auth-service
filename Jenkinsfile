pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    stages {
        stage('Build and Verify') {
            steps {
                sh './mvnw clean verify'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def shortCommit = env.GIT_COMMIT.take(7)
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"
                }

                sh '''
                    echo "Building Docker image: portfolio-auth-service:${IMAGE_TAG}"
                    docker build \
                      -t portfolio-auth-service:${IMAGE_TAG} \
                      .
                '''
            }
        }

        stage('Trivy Security Scan') {
            steps {
                sh '''
                    echo "Scanning Docker image: portfolio-auth-service:${IMAGE_TAG}"

                    trivy image \
                      --scanners vuln \
                      --severity HIGH,CRITICAL \
                      --exit-code 1 \
                      portfolio-auth-service:${IMAGE_TAG}
                '''
            }
        }
    }
}
