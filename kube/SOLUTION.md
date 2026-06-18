# Kubernetes Deployment - Complete Solution

## Problem Solved

Your Kubernetes pods were failing with `ImagePullBackOff` because they were trying to pull `spring-web-app:latest` from a remote registry (Docker Hub), but the image only exists locally.

## Solution Implemented

### 1. Fixed Image Pull Policy
- Updated `kube/mypod.yml` to use `imagePullPolicy: Never`
- This tells Kubernetes to only use locally available images

### 2. Created External Services
Created Kubernetes services that point to your host machine's databases:
- `external-postgres` ? 192.168.18.64:5432
- `external-mongodb` ? 192.168.18.64:27017
- `external-redis` ? 192.168.18.64:6379

Files created:
- `kube/external_postgres_service.yaml`
- `kube/external_mongodb_service.yaml`
- `kube/external_redis_service.yaml`

### 3. Created Production Deployment
- `kube/deployment.yml` - Full deployment with 2 replicas, health checks, resource limits
- `kube/configmap.yml` - Configuration using external service DNS names
- `kube/all-in-one.yaml` - Single file with everything for easy deployment

### 4. Added Makefile Commands
New Kubernetes commands in your Makefile:
```bash
make k8s-build-image    # Build Docker image
make k8s-deploy         # Deploy to Kubernetes
make k8s-delete         # Remove deployment
make k8s-status         # Check pod status
make k8s-logs           # View logs
make k8s-restart        # Restart pods
make k8s-shell          # Open shell in pod
make k8s-port-forward   # Access app locally
make k8s-describe       # Debug pod issues
make k8s-update-ip      # Update host IP
```

### 5. Created Helper Script
`kube/update-host-ip.sh` - Automatically updates host IP across all Kubernetes files

### 6. Documentation
`kube/README.md` - Comprehensive guide with troubleshooting

## How to Deploy

### Quick Start
```bash
# 1. Start databases (if not already running)
make postgres-start
make mongo-start
make redis-start

# 2. Deploy to Kubernetes
make k8s-deploy

# 3. Check status
make k8s-status

# 4. View logs
make k8s-logs

# 5. Access the app
make k8s-port-forward
# Then visit http://localhost:8080
```

### Manual Deployment
```bash
# Build image
docker build -t spring-web-app:latest .

# Deploy everything
kubectl apply -f kube/all-in-one.yaml

# Or deploy individual files
kubectl apply -f kube/external_postgres_service.yaml
kubectl apply -f kube/external_mongodb_service.yaml
kubectl apply -f kube/external_redis_service.yaml
kubectl apply -f kube/configmap.yml
kubectl apply -f kube/deployment.yml
```

## Architecture

```
???????????????????????????????????????
?    Kubernetes Cluster (Rancher)     ?
?                                      ?
?  ??????????????????????????????    ?
?  ?   spring-web-app (Pod 1)   ?    ?
?  ?   imagePullPolicy: Never   ?    ?
?  ??????????????????????????????    ?
?               ?                     ?
?  ??????????????????????????????    ?
?  ?   spring-web-app (Pod 2)   ?    ?
?  ??????????????????????????????    ?
?               ?                     ?
?  ??????????????????????????????    ?
?  ?   external-postgres svc    ?????????
?  ?   external-mongodb svc     ?????????
?  ?   external-redis svc       ?????????
?  ??????????????????????????????    ?  ?
???????????????????????????????????????  ?
                                         ?
         Host Machine (192.168.18.64)   ?
????????????????????????????????????????????
?  PostgreSQL (port 5432) ??????????????????
?  MongoDB    (port 27017) ?????????????????
?  Redis      (port 6379)  ?????????????????
????????????????????????????????????????????
```

## Key Features

### Image Management
- Uses local Docker image (no registry needed)
- `imagePullPolicy: Never` prevents pull attempts
- Build once, deploy immediately

### Database Connectivity
- External services act as Kubernetes-native endpoints
- Pods use DNS names: `external-postgres:5432`
- EndpointSlice routes to host machine
- No hardcoded IPs in ConfigMap

### High Availability
- 2 replica pods for redundancy
- Liveness probes auto-restart unhealthy pods
- Readiness probes control traffic routing
- LoadBalancer service for external access

### Resource Management
- Memory requests: 384Mi, limits: 768Mi
- CPU requests: 250m, limits: 1000m
- Prevents resource starvation

### Health Checks
- Liveness: Restarts if `/actuator/health` fails
- Readiness: Stops traffic if not ready
- Initial delays account for startup time

## Troubleshooting

### ImagePullBackOff Error
**Problem:** Kubernetes tries to pull from remote registry
**Solution:** Ensure `imagePullPolicy: Never` is set

```bash
kubectl describe pod <pod-name>
# Look for: Successfully assigned -> Pulled -> Running
```

### Database Connection Failed
**Problem:** Can't connect to host databases
**Solution:** Check external services

```bash
# Verify external services
kubectl get endpointslices

# Test from pod
kubectl exec -it <pod-name> -- nc -zv external-postgres 5432
```

### Host IP Changed
**Problem:** Network changed, IP is different
**Solution:** Update external services

```bash
# Automatic update
make k8s-update-ip

# Or manual
ipconfig getifaddr en0  # Get new IP
# Edit kube/external_*_service.yaml files
kubectl apply -f kube/external_*_service.yaml
```

### Pod Crashes
**Problem:** Pod restarts repeatedly
**Solution:** Check logs and resources

```bash
# View logs
make k8s-logs

# Check resource usage
kubectl top pods

# Describe pod for events
make k8s-describe
```

## What If My IP Changes?

Your host IP (192.168.18.64) may change if:
- You connect to a different WiFi network
- Your DHCP lease renews
- You restart your network

To update:
```bash
# Run the update script
make k8s-update-ip

# Or manually get IP and update files
ipconfig getifaddr en0
```

## Cleanup

```bash
# Remove all Kubernetes resources
make k8s-delete

# Or manually
kubectl delete -f kube/all-in-one.yaml
```

## Next Steps

### For Development
? You're all set! Just use `make k8s-deploy`

### For Production
Consider:
1. **Container Registry**: Push image to Docker Hub/ECR/GCR
2. **Secrets Management**: Use Kubernetes Secrets for passwords
3. **Database in K8s**: Deploy databases with Helm charts
4. **Ingress**: Replace LoadBalancer with Ingress controller
5. **Monitoring**: Add Prometheus + Grafana
6. **CI/CD**: Automate with GitHub Actions/Jenkins

## Files Created/Modified

### Created:
- `kube/deployment.yml` - Production deployment manifest
- `kube/configmap.yml` - Configuration management
- `kube/namespace.yml` - Optional namespace
- `kube/external_postgres_service.yaml` - PostgreSQL endpoint
- `kube/external_mongodb_service.yaml` - MongoDB endpoint
- `kube/external_redis_service.yaml` - Redis endpoint
- `kube/all-in-one.yaml` - Complete deployment in one file
- `kube/update-host-ip.sh` - IP update helper script
- `kube/README.md` - Comprehensive documentation

### Modified:
- `kube/mypod.yml` - Added `imagePullPolicy: Never`
- `Makefile` - Added 10 new Kubernetes commands

## Summary

You now have a complete Kubernetes deployment solution that:
- ? Uses local Docker images (no registry required)
- ? Connects to host databases via external services
- ? Includes production-ready features (replicas, health checks, resources)
- ? Has comprehensive Makefile commands for easy management
- ? Provides troubleshooting tools and documentation
- ? Can handle IP changes automatically

Run `make help` to see all available commands!

