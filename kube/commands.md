# Kubectl Commands Reference

## Context Management

### View contexts
```bash
# List all contexts
kubectl config get-contexts

# Show current context
kubectl config current-context

# View detailed context info
kubectl config view
```

### Switch contexts
```bash
# Switch to a different context
kubectl config use-context <context-name>

# Example: Switch to rancher-desktop
kubectl config use-context rancher-desktop

# Example: Switch to minikube
kubectl config use-context minikube
```

### Manage contexts
```bash
# Set a new context
kubectl config set-context <context-name> --cluster=<cluster> --user=<user> --namespace=<namespace>

# Delete a context
kubectl config delete-context <context-name>

# Rename a context
kubectl config rename-context <old-name> <new-name>
```

## Namespace Management

### View namespaces
```bash
# List all namespaces
kubectl get namespaces
# or
kubectl get ns

# Show current namespace
kubectl config view --minify --output 'jsonpath={..namespace}'
```

### Switch namespaces
```bash
# Set namespace for current context
kubectl config set-context --current --namespace=<namespace-name>

# Example: Switch to spring-web namespace
kubectl config set-context --current --namespace=spring-web

# Switch back to default namespace
kubectl config set-context --current --namespace=default
```

### Create/Delete namespaces
```bash
# Create a namespace
kubectl create namespace <namespace-name>

# Create from YAML
kubectl apply -f namespace.yml

# Delete a namespace
kubectl delete namespace <namespace-name>
```

### Work with specific namespace (without switching)
```bash
# Get resources in a specific namespace
kubectl get pods -n <namespace-name>
kubectl get services -n <namespace-name>

# Get resources in all namespaces
kubectl get pods --all-namespaces
# or
kubectl get pods -A
```

## Cluster Information

```bash
# View cluster info
kubectl cluster-info

# View node information
kubectl get nodes
kubectl get nodes -o wide

# Check API resources
kubectl api-resources
```

## Common Workflows

### Check current context and namespace
```bash
kubectl config current-context
kubectl config view --minify --output 'jsonpath={..namespace}'
```

### Quick context switch
```bash
# Save this as an alias in ~/.bashrc or ~/.zshrc
alias kctx='kubectl config use-context'
alias kns='kubectl config set-context --current --namespace'

# Usage:
# kctx rancher-desktop
# kns spring-web
```

## Project Specific

### Spring Web App Setup
```bash
# Set context (if needed)
kubectl config use-context rancher-desktop

# Create spring-web namespace
kubectl create namespace spring-web

# Switch to spring-web namespace
kubectl config set-context --current --namespace=spring-web

# Deploy external services
kubectl apply -f kube/external-service/

# Deploy application
kubectl apply -f kube/deployment/deployment.yml
kubectl apply -f kube/service.yml

# Check status
kubectl get pods
kubectl get services
```

### Nginx frontend (static files + reverse proxy)
```bash
# Config + static content
kubectl apply -f kube/nginx-configmap.yml

# Pod and NodePort service
kubectl apply -f kube/nginx-pod.yml
kubectl apply -f kube/service/nginx_service.yml

# Static page served by nginx
curl http://localhost:30081/

# Proxied to spring-web-service (strips the /api prefix)
curl http://localhost:30081/api/pets/type/DOG

# Reload after editing the config map
kubectl delete pod nginx -n spring-web && kubectl apply -f kube/nginx-pod.yml
```

> Note: nginx resolves `spring-web-service` at startup, so apply
> `kube/service/spring-web_service.yml` before the nginx pod.
