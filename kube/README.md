# Kubernetes Deployment Guide

This guide explains how to deploy the Spring Web application to Kubernetes (Rancher Desktop).

## Prerequisites

- Rancher Desktop installed and running
- Docker daemon running
- kubectl configured to point to your Rancher Desktop cluster
- Databases running locally (PostgreSQL, MongoDB, Redis)

## Quick Start

### 1. Deploy to Kubernetes

```bash
make k8s-deploy
```

This command will:
- Build the Docker image
- Create/update ConfigMap with environment variables
- Deploy the application with 2 replicas
- Create a LoadBalancer service

### 2. Check Status

```bash
make k8s-status
```

### 3. View Logs

```bash
make k8s-logs
```

### 4. Access the Application

```bash
make k8s-port-forward
```

Then open http://localhost:8080 in your browser.

## External Services Explained

### What are External Services?

External services allow Kubernetes pods to connect to services running outside the cluster (like databases on your host machine).

### How it Works

1. **Service Definition**: Creates a Kubernetes service with a stable DNS name
   - `external-postgres` ? accessible at `external-postgres:5432` from pods
   - `external-mongodb` ? accessible at `external-mongodb:27017` from pods
   - `external-redis` ? accessible at `external-redis:6379` from pods

2. **EndpointSlice**: Points the service to your host IP (192.168.18.64)
   - Kubernetes routes traffic from pods to your host machine

3. **ConfigMap**: App uses service DNS names instead of IPs
   - Makes the configuration portable and DNS-based

### Updating Your Host IP

If your host IP changes, update the following files:
- `kube/external_postgres_service.yaml`
- `kube/external_mongodb_service.yaml`
- `kube/external_redis_service.yaml`
- `kube/all-in-one.yaml`

Or run this command to get your current IP:
```bash
ipconfig getifaddr en0
```

Then redeploy:
```bash
kubectl apply -f kube/external_*_service.yaml
```

## Available Make Commands

| Command | Description |
|---------|-------------|
| `make k8s-build-image` | Build Docker image for Kubernetes |
| `make k8s-deploy` | Deploy app to Kubernetes |
| `make k8s-delete` | Delete Kubernetes deployment |
| `make k8s-status` | Show pod and service status |
| `make k8s-logs` | View real-time pod logs |
| `make k8s-restart` | Restart deployment (rollout restart) |
| `make k8s-shell` | Open shell in a pod |
| `make k8s-port-forward` | Port forward to access app locally |
| `make k8s-describe` | Show detailed pod information |

## Manual Deployment

If you prefer to use kubectl directly:

### Option 1: Individual Files

```bash
# Build image
docker build -t spring-web-app:latest .

# Apply configurations
kubectl apply -f kube/external_postgres_service.yaml
kubectl apply -f kube/external_mongodb_service.yaml
kubectl apply -f kube/external_redis_service.yaml
kubectl apply -f kube/configmap.yml
kubectl apply -f kube/deployment.yml

# Check status
kubectl get pods -l app=spring-web
kubectl get svc spring-web

# View logs
kubectl logs -l app=spring-web -f
```

### Option 2: All-in-One File

```bash
# Build image
docker build -t spring-web-app:latest .

# Deploy everything at once
kubectl apply -f kube/all-in-one.yaml

# Check status
kubectl get all -l app=spring-web
```

## Configuration

### Environment Variables

The application configuration is stored in `kube/configmap.yml`. Update this file to change:
- Database connection strings
- MongoDB host/port
- Redis host/port

After updating, redeploy:

```bash
kubectl apply -f kube/configmap.yml
make k8s-restart
```

### Resource Limits

The deployment has the following resource limits:

**Requests:**
- Memory: 384Mi
- CPU: 250m

**Limits:**
- Memory: 768Mi
- CPU: 1000m

Update these in `kube/deployment.yml` if needed.

## Troubleshooting

### Image Pull Issues

If you see `ImagePullBackOff` errors:

1. Ensure `imagePullPolicy: Never` is set in your deployment
2. Rebuild the image: `make k8s-build-image`
3. Redeploy: `make k8s-deploy`

### Pod Not Starting

```bash
# Check pod details
make k8s-describe

# Check logs
make k8s-logs

# Check events
kubectl get events --sort-by='.lastTimestamp'
```

### Database Connection Issues

1. Ensure databases are running locally:
   ```bash
   make postgres-status
   make mongo-status
   make redis-status
   ```

2. Start databases if needed:
   ```bash
   make postgres-start
   make mongo-start
   make redis-start
   ```

3. Verify external services are pointing to the correct host IP:
   ```bash
   kubectl get endpointslices
   ```

4. Test connectivity from a pod:
   ```bash
   # Get a shell in a pod
   make k8s-shell
   
   # Test database connections
   nc -zv external-postgres 5432
   nc -zv external-mongodb 27017
   nc -zv external-redis 6379
   ```

5. If your host IP changed, update the external service files and redeploy:
   ```bash
   # Get your current IP
   ipconfig getifaddr en0
   
   # Update the YAML files with the new IP
   # Then reapply
   kubectl apply -f kube/external_*_service.yaml
   ```

### Pod Crashes or Restarts

```bash
# Check pod logs
make k8s-logs

# Check pod description for restart reasons
make k8s-describe

# Check resource usage
kubectl top pods -l app=spring-web
```

## Scaling

Scale the deployment to more/fewer replicas:

```bash
kubectl scale deployment/spring-web --replicas=3
```

## Cleanup

To remove all Kubernetes resources:

```bash
make k8s-delete
```

## Using Simple Pod (mypod.yml)

For testing, you can also deploy a simple pod:

```bash
kubectl apply -f kube/mypod.yml
kubectl get pod spring-web
kubectl logs spring-web -f
```

Delete when done:
```bash
kubectl delete -f kube/mypod.yml
```

## Health Checks

The deployment includes:
- **Liveness probe**: Checks if app is alive (restarts if fails)
- **Readiness probe**: Checks if app is ready to receive traffic

Both probes use the `/actuator/health` endpoint.

## Accessing Services

### LoadBalancer Service

The service is exposed as type `LoadBalancer`:

```bash
kubectl get svc spring-web
```

For Rancher Desktop, you can access using `localhost:8080` or use port-forward:

```bash
make k8s-port-forward
```

## Production Considerations

For production deployments, consider:

1. **Use a container registry** (Docker Hub, ECR, GCR, etc.)
   - Push image to registry
   - Update deployment to pull from registry
   - Remove `imagePullPolicy: Never`

2. **Use Secrets for sensitive data**
   - Move passwords to Kubernetes Secrets
   - Don't use ConfigMaps for sensitive data

3. **Deploy databases in Kubernetes**
   - Use Helm charts for PostgreSQL, MongoDB, Redis
   - Or use managed cloud services

4. **Add resource monitoring**
   - Install Prometheus and Grafana
   - Set up alerts

5. **Configure ingress**
   - Use Ingress controller for better routing
   - Add SSL/TLS certificates

6. **Use namespaces**
   - Deploy to a dedicated namespace
   - Apply `namespace.yml` first

