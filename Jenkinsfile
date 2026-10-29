// =============================================================================
// Jenkinsfile — Automated Court Hearing Reminder Portal
// Declarative CI Pipeline with Visible Test Reports
// =============================================================================
pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        nodejs 'node-18' // Must match the name defined in Manage Jenkins -> Tools
    }

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: '1.0.0', description: 'Docker image tag')
        booleanParam(name: 'BUILD_DOCKER', defaultValue: true, description: 'Build Docker images using Docker Compose?')
    }

    environment {
        COMPOSE_FILE = 'docker/docker-compose.yml'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }

    stages {
        // ── Stage 1: Checkout ────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "==> Stage 1: Checkout Source Code"
                checkout scm
                script {
                    if (isUnix()) {
                        sh 'git log -1 --oneline || true'
                    } else {
                        bat 'git log -1 --oneline || ver'
                    }
                }
            }
        }

        // ── Stage 2: Validate ────────────────────────────────────────────────
        stage('Validate') {
            steps {
                echo "==> Stage 2: Validate Environment & Project Files"
                script {
                    if (isUnix()) {
                        sh '''
                            echo "=== Validating Required Project Files ==="
                            test -f backend/pom.xml || { echo "ERROR: backend/pom.xml missing"; exit 1; }
                            test -f frontend/package.json || { echo "ERROR: frontend/package.json missing"; exit 1; }
                            test -f docker/docker-compose.yml || { echo "ERROR: docker/docker-compose.yml missing"; exit 1; }
                            echo "All core project files verified."

                            echo "=== Tool Environment ==="
                            java -version 2>&1 || true
                            mvn -version 2>&1 || true
                            node -v 2>&1 || true
                            npm -v 2>&1 || true
                            docker --version 2>&1 || true
                        '''
                    } else {
                        bat '''
                            echo "=== Validating Required Project Files ==="
                            if not exist backend\\pom.xml (echo ERROR: backend\\pom.xml missing & exit /b 1)
                            if not exist frontend\\package.json (echo ERROR: frontend\\package.json missing & exit /b 1)
                            if not exist docker\\docker-compose.yml (echo ERROR: docker\\docker-compose.yml missing & exit /b 1)
                            echo All core project files verified.

                            echo "=== Tool Environment ==="
                            java -version
                            mvn -version
                            node -v
                            npm -v
                            docker --version
                        '''
                    }
                }
            }
        }

        // ── Stage 3: Backend Build ───────────────────────────────────────────
        stage('Backend Build') {
            steps {
                echo "==> Stage 3: Compile Spring Boot Backend (Java 17 + Maven)"
                dir('backend') {
                    script {
                        if (isUnix()) {
                            sh 'mvn clean compile -B -DskipTests'
                        } else {
                            bat 'mvn clean compile -B -DskipTests'
                        }
                    }
                }
            }
        }

        // ── Stage 4: Backend Tests ───────────────────────────────────────────
        stage('Backend Tests') {
            steps {
                echo "==> Stage 4: Execute Backend Unit & Integration Tests (JUnit 5)"
                dir('backend') {
                    script {
                        // Failures must NOT be swallowed — pipeline fails if tests fail
                        if (isUnix()) {
                            sh 'mvn test -B'
                        } else {
                            bat 'mvn test -B'
                        }
                    }
                }
            }
        }

        // ── Stage 5: Frontend Install ────────────────────────────────────────
        stage('Frontend Install') {
            steps {
                echo "==> Stage 5: Install Frontend Dependencies (Lockfile Reproducible)"
                dir('frontend') {
                    script {
                        if (isUnix()) {
                            sh 'npm ci || npm install --prefer-offline'
                        } else {
                            bat 'npm ci || npm install --prefer-offline'
                        }
                    }
                }
            }
        }

        // ── Stage 6: Frontend Tests ──────────────────────────────────────────
        stage('Frontend Tests') {
            steps {
                echo "==> Stage 6: Execute Frontend Test Suite (Generating JUnit XML)"
                dir('frontend') {
                    script {
                        if (isUnix()) {
                            sh 'npm test'
                        } else {
                            bat 'npm test'
                        }
                    }
                }
            }
        }

        // ── Stage 7: Frontend Build ──────────────────────────────────────────
        stage('Frontend Build') {
            steps {
                echo "==> Stage 7: Build Production Frontend Assets (Vite)"
                dir('frontend') {
                    script {
                        if (isUnix()) {
                            sh 'npm run build'
                        } else {
                            bat 'npm run build'
                        }
                    }
                }
            }
        }

        // ── Stage 8: Docker Build ────────────────────────────────────────────
        stage('Docker Build') {
            when {
                expression { params.BUILD_DOCKER }
            }
            steps {
                echo "==> Stage 8: Build Frontend & Backend Images with Docker Compose"
                script {
                    if (isUnix()) {
                        sh 'docker compose -f ${COMPOSE_FILE} build'
                    } else {
                        bat 'docker compose -f %COMPOSE_FILE% build'
                    }
                }
            }
        }
    }

    // ── Test Reports & Artifact Archival ─────────────────────────────────────
    post {
        always {
            echo "==> Publishing Visible Automated Test Reports in Jenkins"
            // Visible JUnit XML report generation for Jenkins UI Test Result Trend
            junit testResults: 'backend/target/surefire-reports/TEST-*.xml,frontend/test-results/*.xml',
                  allowEmptyResults: false

            // Archive distributable artifacts
            archiveArtifacts artifacts: 'backend/target/*.jar,frontend/dist/**',
                             allowEmptyArchive: true,
                             fingerprint: true
        }
        success {
            echo "SUCCESS: Pipeline completed with all tests passing!"
        }
        failure {
            echo "FAILURE: Build or tests failed. Inspect the Jenkins Test Results graph and stage logs for root cause."
        }
    }
}
