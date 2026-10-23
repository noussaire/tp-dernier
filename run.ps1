param([ValidateSet('test','run','package')][string]$Action='run')
$ErrorActionPreference='Stop'
if (Test-Path 'C:\Program Files\Java\jdk-23\bin\java.exe') {
    $env:JAVA_HOME='C:\Program Files\Java\jdk-23'
    $env:Path="$env:JAVA_HOME\bin;$env:Path"
}
switch ($Action) {
    'test' { & mvn test }
    'run' { & mvn compile exec:java }
    'package' { & mvn clean package }
}
exit $LASTEXITCODE
