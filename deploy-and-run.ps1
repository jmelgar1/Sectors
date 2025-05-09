# Build the plugin and deploy it to the server
Write-Host "Building and deploying plugin..." -ForegroundColor Green
./gradlew build

# Copy the plugin JAR to the run/plugins directory
Write-Host "Copying plugin to server..." -ForegroundColor Green
Copy-Item -Path "build/libs/Sectors-unspecified-reobf.jar" -Destination "run/plugins/Sectors.jar" -Force

# Start the server
Write-Host "Starting server..." -ForegroundColor Cyan
Set-Location -Path "run"
java -Xms1G -Xmx2G -jar paper.jar nogui 