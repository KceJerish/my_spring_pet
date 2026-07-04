# Docker Layer Caching - Commands & Tips

## Check Docker Layer History

### View image layers
```bash
# Show layers of an image
docker history spring-web-app:latest

# Show layers with full command (no truncation)
docker history --no-trunc spring-web-app:latest

# Show layers with human-readable sizes
docker history --human spring-web-app:latest

# Show layers in JSON format
docker history --format "{{.ID}}\t{{.CreatedBy}}\t{{.Size}}" spring-web-app:latest
```

### Inspect image details
```bash
# Full image details including layers
docker inspect spring-web-app:latest

# Extract just the layers
docker inspect spring-web-app:latest | jq '.[0].RootFS.Layers'

# Check image size breakdown
docker inspect spring-web-app:latest | jq '.[0].Size'
```

## Build with Cache Information

### Build and see cache usage
```bash
# Build with progress output (see which layers use cache)
docker build -t spring-web-app:latest .

# Build with detailed progress (buildkit)
docker build --progress=plain -t spring-web-app:latest .

# Build without using cache (force rebuild all layers)
docker build --no-cache -t spring-web-app:latest .

# Build and invalidate cache from a specific layer onwards
docker build --no-cache-filter build -t spring-web-app:latest .
```

### During build, you'll see:
- `CACHED` - Layer was reused from cache
- `RUN` - Layer was rebuilt

## List Cached Layers

```bash
# List all layers/images (including intermediate)
docker images -a

# List dangling images (unused intermediate layers)
docker images -f "dangling=true"

# Show system-wide disk usage (includes cache)
docker system df

# Show detailed disk usage
docker system df -v
```

## Understanding Your Dockerfile Caching

### Your current Dockerfile cache strategy:

**Build Stage (Layers from top to bottom):**
1. `FROM maven:3.9-eclipse-temurin-25` - ✅ Cached (base image)
2. `WORKDIR /app` - ✅ Cached
3. `COPY pom.xml .` - ✅ Cached if pom.xml unchanged
4. `RUN mvn dependency:go-offline` - ✅ Cached if pom.xml unchanged
5. `COPY src ./src` - ⚠️ Cache invalidated if any source code changes
6. `RUN mvn clean package` - ⚠️ Rebuilds if source code changed

**Runtime Stage (Layers):**
7. `FROM eclipse-temurin:25-jre-alpine` - ✅ Cached (base image)
8. `WORKDIR /app` - ✅ Cached
9. `RUN addgroup/adduser` - ✅ Cached
10. `COPY --from=build` - ⚠️ New if build stage changed
11. `RUN chown` - ⚠️ New if jar changed
12. `USER spring` - ✅ Cached
13. `EXPOSE 8080` - ✅ Cached (metadata only)
14. `HEALTHCHECK` - ✅ Cached (metadata only)
15. `ENTRYPOINT` - ✅ Cached (metadata only)

## Cache Optimization Tips

### Good practices in your Dockerfile ✅
1. **Dependencies first**: `COPY pom.xml` before `COPY src` - dependencies cached separately
2. **Multi-stage build**: Build artifacts don't include build tools in final image
3. **Specific copies**: Copy only what's needed at each stage

### Further optimizations
```dockerfile
# Split dependencies by change frequency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# If you have other config files that rarely change
COPY .mvn .mvn
COPY mvnw .
```

## Practical Examples

### Check what was cached in last build
```bash
docker build --progress=plain -t spring-web-app:latest . 2>&1 | grep -E "CACHED|RUN"
```

### Compare two builds
```bash
# First build
docker build -t spring-web-app:v1 .
docker history spring-web-app:v1 > build1.txt

# Second build (after changes)
docker build -t spring-web-app:v2 .
docker history spring-web-app:v2 > build2.txt

# Compare
diff build1.txt build2.txt
```

### Clean up cache
```bash
# Remove unused build cache
docker builder prune

# Remove all build cache (force clean rebuild next time)
docker builder prune -a

# Remove dangling images
docker image prune
```

## Troubleshooting Cache Issues

### Cache not being used when it should
```bash
# Check BuildKit is enabled (better caching)
export DOCKER_BUILDKIT=1

# Use inline cache
docker build --cache-from spring-web-app:latest -t spring-web-app:latest .
```

### Force rebuild specific stage
```bash
# Rebuild from specific stage
docker build --target build --no-cache -t temp-build .
docker build -t spring-web-app:latest .
```

## Monitor Cache During Build

```bash
# Watch build with timestamps
docker build --progress=plain -t spring-web-app:latest . | ts

# Count cached vs rebuilt layers
docker build --progress=plain -t spring-web-app:latest . 2>&1 | \
  grep -c "CACHED" && \
  grep -c "RUN"
```
