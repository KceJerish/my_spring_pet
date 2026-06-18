#!/bin/bash
# Script to update host IP in all Kubernetes external service files

set -e

# Get current host IP
CURRENT_IP=$(ipconfig getifaddr en0 || ipconfig getifaddr en1 || echo "")

if [ -z "$CURRENT_IP" ]; then
    echo "? Could not detect host IP address"
    echo "Please run: ipconfig getifaddr en0"
    exit 1
fi

echo "? Detected host IP: $CURRENT_IP"
echo ""

# Get old IP from existing file
OLD_IP=$(grep -oE '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' kube/external_postgres_service.yaml | head -1)

if [ "$OLD_IP" = "$CURRENT_IP" ]; then
    echo "? IP address is already up to date ($CURRENT_IP)"
    exit 0
fi

echo "? Updating IP from $OLD_IP to $CURRENT_IP in Kubernetes files..."
echo ""

# Update external service files
sed -i.bak "s/$OLD_IP/$CURRENT_IP/g" kube/external_postgres_service.yaml
sed -i.bak "s/$OLD_IP/$CURRENT_IP/g" kube/external_mongodb_service.yaml
sed -i.bak "s/$OLD_IP/$CURRENT_IP/g" kube/external_redis_service.yaml
sed -i.bak "s/$OLD_IP/$CURRENT_IP/g" kube/all-in-one.yaml

# Remove backup files
rm -f kube/*.bak

echo "? Updated the following files:"
echo "   - kube/external_postgres_service.yaml"
echo "   - kube/external_mongodb_service.yaml"
echo "   - kube/external_redis_service.yaml"
echo "   - kube/all-in-one.yaml"
echo ""
echo "To apply changes to running cluster, run:"
echo "   kubectl apply -f kube/external_postgres_service.yaml"
echo "   kubectl apply -f kube/external_mongodb_service.yaml"
echo "   kubectl apply -f kube/external_redis_service.yaml"
echo ""
echo "Or redeploy everything:"
echo "   make k8s-delete && make k8s-deploy"

