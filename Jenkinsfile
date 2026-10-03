// ============================================================
// Jenkinsfile — Court Hearing Reminder Portal
// Declarative Pipeline
// ============================================================
pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG',    defaultValue: '1.0.0', description: 'Docker image tag to build/push')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false, description: 'Push images to Docker Hub?')
        booleanParam(name: 'DEPLOY',     defaultValue: false, description: 'Deploy after build?')
        string(name: 'APP_URL', defaultValue: 'http://localhost', description: 'Application base URL for Selenium tests')
    }

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub-credentials')   // Jenkins credential ID
        DOCKER_HUB_USERNAME    = "${DOCKER_HUB_CREDENTIALS_USR}"
        BACKEND_IMAGE          = "${DOCKER_HUB_USERNAME}/court-backend:${params.IMAGE_TAG}"
        FRONTEND_IMAGE         = "${DOCKER_HUB_USERNAME}/court-frontend:${params.IMAGE_TAG}"
        COMPOSE_FILE           = "docker/docker-compose.yml"
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        // ── Stage 1: Checkout ───────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -5'
                echo "Branch: ${env.BRANCH_NAME ?: 'unknown'}"
            }
        }

        // ── Stage 2: Backend Build (Docker multi-stage) ─────────
        stage('Backend Build') {
            steps {
                sh """
                    echo "==> Building backend Docker image: ${BACKEND_IMAGE}"
                    docker build -t ${BACKEND_IMAGE} ./backend
                    docker tag ${BACKEND_IMAGE} local/court-backend:latest
                """
            }
        }

        // ── Stage 3: Unit Tests (run inside backend image) ──────
        stage('Unit Tests') {
            steps {
                sh """
                    echo "==> Running unit tests inside builder stage"
                    docker run --rm \\
                        --entrypoint mvn \\
                        maven:3.9.6-eclipse-temurin-17 \\
                        -v \$(pwd)/backend:/app \\
                        -w /app \\
                        test -Dspring.datasource.url=jdbc:h2:mem:testdb -Dspring.datasource.driver-class-name=org.h2.Driver || true
                """
                // Alternative: extract test results from build
                junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        // ── Stage 4: Frontend Build ─────────────────────────────
        stage('Frontend Build') {
            steps {
                sh """
                    echo "==> Building frontend Docker image: ${FRONTEND_IMAGE}"
                    docker build -t ${FRONTEND_IMAGE} ./frontend
                    docker tag ${FRONTEND_IMAGE} local/court-frontend:latest
                """
            }
        }

        // ── Stage 5: Docker Compose Up (integration env) ────────
        stage('Start Integration Environment') {
            when {
                expression { params.DEPLOY || currentBuild.result == null }
            }
            steps {
                sh """
                    cp .env.example .env 2>/dev/null || true
                    IMAGE_TAG=${params.IMAGE_TAG} \\
                    DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME} \\
                    docker compose -f ${COMPOSE_FILE} up -d --no-build
                    echo "==> Waiting for services..."
                    sleep 30
                """
            }
        }

        // ── Stage 6: Selenium Tests ─────────────────────────────
        stage('Selenium Tests') {
            steps {
                sh """
                    echo "==> Running Selenium integration tests..."
                    cd tests/selenium
                    mvn verify -Pselenium \\
                        -Dapp.base.url=${params.APP_URL} \\
                        -Dfailsafe.rerunFailingTestsCount=1 || true
                """
            }
            post {
                always {
                    junit testResults: 'tests/selenium/target/failsafe-reports/*.xml',
                          allowEmptyResults: true
                    archiveArtifacts artifacts: 'tests/selenium/target/screenshots/**/*.png',
                                     allowEmptyArchive: true
                }
            }
        }

        // ── Stage 7: Docker Push ────────────────────────────────
        stage('Docker Push') {
            when { expression { params.PUSH_IMAGE } }
            steps {
                sh """
                    echo "${DOCKER_HUB_CREDENTIALS_PSW}" | docker login -u "${DOCKER_HUB_CREDENTIALS_USR}" --password-stdin
                    docker push ${BACKEND_IMAGE}
                    docker push ${FRONTEND_IMAGE}
                    # Also tag and push latest
                    docker tag ${BACKEND_IMAGE}  ${DOCKER_HUB_USERNAME}/court-backend:latest
                    docker tag ${FRONTEND_IMAGE} ${DOCKER_HUB_USERNAME}/court-frontend:latest
                    docker push ${DOCKER_HUB_USERNAME}/court-backend:latest
                    docker push ${DOCKER_HUB_USERNAME}/court-frontend:latest
                    docker logout
                """
            }
        }

        // ── Stage 8: Deploy ─────────────────────────────────────
        stage('Deploy') {
            when { expression { params.DEPLOY } }
            steps {
                sh """
                    chmod +x scripts/deploy.sh scripts/healthcheck.sh
                    HEALTH_URL=${params.APP_URL}/api/health \\
                    ./scripts/deploy.sh ${params.IMAGE_TAG}
                """
            }
        }

        // ── Stage 9: Health Check ───────────────────────────────
        stage('Health Check') {
            when { expression { params.DEPLOY } }
            steps {
                sh """
                    chmod +x scripts/healthcheck.sh
                    ./scripts/healthcheck.sh ${params.APP_URL}/api/health 20 5
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        failure {
            echo "❌ Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        always {
            // Tear down integration environment
            sh """
                docker compose -f ${COMPOSE_FILE} down --remove-orphans 2>/dev/null || true
            """
        }
    }
}
