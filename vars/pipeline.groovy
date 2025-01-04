def call() {
    echo 'Checking out code...'
    checkout scm

    echo 'Setting up Java 17...'
    sh 'sudo apt update'
    sh 'sudo apt install -y openjdk-17-jdk'
    
    echo 'Setting up Maven...'
    sh 'sudo apt install -y maven'
    
    echo 'Building project with Maven...'
    sh 'mvn clean package'
    
    def buildTag = "build-${env.BUILD_NUMBER}"
    sh "git tag ${buildTag}"
    sh "git push origin ${buildTag}"
    
    echo 'Uploading artifact...'
    archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
    
    echo 'Running Spring Boot application...'
    sh 'nohup mvn spring-boot:run &'

    def appRunning = false
    (1..10).each { attempt ->
        def response = sh(script: 'curl --write-out "%{http_code}" --silent --output /dev/null http://localhost:8080', returnStdout: true).trim()
        if (response == "200") {
            appRunning = true
            break
        }
        sleep(time: 5, unit: 'SECONDS')
    }
    if (!appRunning) {
        error("The app did not start correctly!")
    }

    def publicIp
    try {
        publicIp = sh(script: "curl -s https://checkip.amazonaws.com", returnStdout: true).trim()
    } catch (Exception e) {
        echo "Could not retrieve public IP. Defaulting to localhost."
        publicIp = "localhost"
    }
    echo "The application is running and accessible at: http://${publicIp}:8080"

      echo 'Validating that the app is running...'
    def response = sh(script: 'curl --write-out "%{http_code}" --silent --output /dev/null http://localhost:8080', returnStdout: true).trim()
    if (response == "200") {
        echo 'The app is running successfully!'
    } else {
        echo "The app failed to start. HTTP response code: ${response}"
        error("The app did not start correctly!")
    }

   echo 'Gracefully stopping the Spring Boot application...'
    sh 'mvn spring-boot:stop'
}
