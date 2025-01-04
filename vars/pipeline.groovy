def checkscm() {
    echo 'Checking out code...'
    checkout scm
}
def setupjava() {
    echo 'Setting up Java 17...'
    sh 'sudo apt update'
    sh 'sudo apt install -y openjdk-17-jdk'
}
def mavensetup() {
    echo 'Setting up Maven...'
    sh 'sudo apt install -y maven'
}
def build() {
    echo 'Building project with Maven...'
    sh 'mvn clean package'
}
def runApp() {
    echo 'Running Spring Boot application...'
    sh 'nohup mvn spring-boot:run &'
    sleep(time: 15, unit: 'SECONDS')

    def publicIp = sh(script: "curl -s https://checkip.amazonaws.com", returnStdout: true).trim()
    echo "The application is running and accessible at: http://${publicIp}:8080"
}
def validateApp() {
    echo 'Validating that the app is running...'
    def response = sh(script: 'curl --write-out "%{http_code}" --silent --output /dev/null http://localhost:8080', returnStdout: true).trim()
    if (response == "200") {
        echo 'The app is running successfully!'
    } else {
        echo "The app failed to start. HTTP response code: ${response}"
        error("The app did not start correctly!")
    }
}
def waiting() {
        echo 'Waiting for 2 minutes...'
        sleep(time: 2, unit: 'MINUTES')  // Wait for 2 minutes
}
def stop() {
    echo 'Gracefully stopping the Spring Boot application...'
    sh 'mvn spring-boot:stop'
}
def clean() {
    echo 'Cleaning up...'
    sh 'pkill -f "mvn spring-boot:run" || true'
}
