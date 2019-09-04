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
        image: eu.gcr.io/xebikart-dev-1/xebikart-state-api:${CIRCLE_SHA1}
        imagePullPolicy: Always
        env:
        - name: AMQP_HOST
          value: "rabbitmq.xebik.art"
        - name: AMQP_PORT
          value: "1883"
        - name: AMQP_USERNAME
          value: "xebikart1"
        - name: AMQP_PASSWORD
          value: "xebikart1"
        resources:
          requests:
            cpu: 100m
            memory: 1000Mi
        ports:
          - containerPort: 80
