pipeline {
    agent {
      kubernetes {
        // Change the name of jenkins-maven label to be able to use yaml configuration snippet
        label "maven-dind"
        // Inherit from Jx Maven pod template
        inheritFrom "jenkins-maven-java11"
        // Add pod configuration to Jenkins builder pod template
        yaml '''
spec:
  volumes:
    - name: docker-daemon
      emptyDir: {}
    - name: jenkins-docker-cfg-volume
      secret:
        secretName: jenkins-docker-cfg
  containers:
    # Override Docker config in Maven to use DinD
    - name: maven
      env:
        - name: DOCKER_HOST
          value: tcp://localhost:2375
      volumeMounts:
        - mountPath: /var/lib/docker
          name: docker-daemon
    # Create sidecar container with DinD to build docker images
    - name: dind
      image: docker:18.06.1-dind
      imagePullPolicy: IfNotPresent
      args:
        - --registry-mirror=https://mirror.gcr.io
      workingDir: /home/jenkins
      securityContext:
        privileged: true
      tty: true
      env:
        - name: DOCKER_CONFIG
          value: /home/jenkins/.docker/
        - name: DOCKER_HOST
          value: tcp://localhost:2375
        - name: DOCKER_REGISTRY
          valueFrom:
            configMapKeyRef:
              name: jenkins-x-docker-registry
              key: docker.registry
      resources:
        requests:
          cpu: 350m
          memory: 600Mi
        limits:
          cpu: 1
          memory: 2048Mi
      volumeMounts:
        - mountPath: /home/jenkins
          name: workspace-volume
        - mountPath: /var/lib/docker
          name: docker-daemon
        - mountPath: /home/jenkins/.docker
          name: jenkins-docker-cfg-volume
          readOnly: true
'''
      }
    }
    environment {
      DOCKER_REGISTRY   = 'gcr.io'
      ORG               = 'activiti-cloud-blueprint'
      APP_NAME          = 'example-runtime-bundle'
      CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    }
    stages {
      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
        }
        environment {
          PREVIEW_VERSION = "7.1.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        }
        steps {
          container('maven') {
            sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
            //add DskipTests since not clear how to fix test fast  
            sh "mvn install"

            //sh 'mvn deploy -DskipTests'

            sh 'export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml'
          }

        }
      }
      stage('Build Release') {
        when {
          branch 'develop'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout develop"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$(jx-release-version) > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
            sh "mvn clean verify -DskipITs=false"

            retry(5){
	          dir ("./charts/runtime-bundle") {
	            retry(5){  
	              sh "make tag"
	            }
	          }              
            }
            
            sh 'mvn clean deploy -DskipTests'

            sh 'export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml'
            
            dir ("./charts/runtime-bundle") {
	          retry(5){  
	            sh 'make release'
	          }
	        }              
     
            retry(2){
              sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies \$(cat VERSION)"
              sh "rm -rf .updatebot-repos/"
              sh "sleep \$((RANDOM % 10))"
              sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies \$(cat VERSION)"
            }
            
          }
        }
      }
      stage('Build Release from Tag') {
        when {
          tag '*RELEASE'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout $TAG_NAME"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$TAG_NAME > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
          }
          container('maven') {
            sh '''
              mvn clean deploy -P !alfresco -P central
              '''

            sh 'export VERSION=`cat VERSION`'// && skaffold build -f skaffold.yaml'

            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            //sh "updatebot push"
            //sh "updatebot update"

            sh "echo pushing with update using version \$(cat VERSION)"

            sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies \$(cat VERSION)"
          }
        }
      }
    }
    post {
        always {
            cleanWs()
        }
    }
  }
