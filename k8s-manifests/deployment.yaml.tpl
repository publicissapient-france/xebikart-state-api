apiVersion: apps/v1
kind: Deployment
metadata:
  name: xebikart-state-api
spec:
  selector:
    matchLabels:
      app: xebikart
      tier: state-api
  replicas: 1
  template:
    metadata:
      labels:
        app: xebikart
        tier: state-api
    spec:
      containers:
      - name: state-api
        image: eu.gcr.io/xebikart-dev-1/xebikart-state-api:@@DOCKER_TAG@@
        imagePullPolicy: Always
        resources:
          requests:
            cpu: 100m
            memory: 1000Mi
        ports:
          - containerPort: 80
