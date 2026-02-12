pipeline {
  agent any
  options { timestamps() }
  environment {
    CODEQL_VERSION = "2.24.1"
    CODEQL_DIR = "${WORKSPACE}/codeql"
    CODEQL_RAM = "4096"
    NODE_VERSION = "20.18.1"
    NODE_DIR = "${WORKSPACE}/node"
    
    // Quality Gate thresholds
    MAX_CRITICAL_ISSUES = "0"
    MAX_HIGH_ISSUES = "5"
    MAX_MEDIUM_ISSUES = "20"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Verify toolchain') {
      steps {
        sh '''
          set -eux
          whoami
          id
          java -version
          javac -version
          git --version
          curl --version
          unzip -v | head -n 1
          node --version || true
          npm --version || true
        '''
      }
    }
    stage('Install Node.js') {
      steps {
        sh '''
          set -eux
          if [ -x "${NODE_DIR}/bin/node" ]; then
            echo "Node.js is already installed in workspace: $(${NODE_DIR}/bin/node --version)"
          else
            echo "Installing Node.js ${NODE_VERSION}..."
            rm -f node.tar.gz
            curl -fL --retry 5 --retry-delay 2 \
              -o node.tar.gz \
              "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.gz"
            ls -lh node.tar.gz
            rm -rf "${NODE_DIR}"
            mkdir -p "${NODE_DIR}"
            tar -xzf node.tar.gz -C "${NODE_DIR}" --strip-components=1
            rm -f node.tar.gz
          fi
          export PATH="${NODE_DIR}/bin:$PATH"
          echo "Node.js version: $(node --version)"
          echo "npm version: $(npm --version)"
        '''
      }
    }
    stage('Download CodeQL CLI') {
      steps {
        sh '''
          set -eux
          rm -f codeql.zip
          curl -fL --retry 5 --retry-delay 2 \
            -o codeql.zip \
            "https://github.com/github/codeql-cli-binaries/releases/download/v${CODEQL_VERSION}/codeql-linux64.zip"
          ls -lh codeql.zip
          head -c 200 codeql.zip || true
          rm -rf "${CODEQL_DIR}"
          unzip -q codeql.zip -d "${WORKSPACE}"
          "${CODEQL_DIR}/codeql" version
        '''
      }
    }
    stage('Download CodeQL Query Packs') {
      steps {
        sh '''
          set -eux
          "${CODEQL_DIR}/codeql" pack download codeql/java-queries
          "${CODEQL_DIR}/codeql" pack download codeql/javascript-queries
          "${CODEQL_DIR}/codeql" resolve packs
          "${CODEQL_DIR}/codeql" resolve languages
        '''
      }
    }
    stage('Build (best effort)') {
      steps {
        sh '''
          set -eux
          for d in auth gateway registry user; do
            if [ -x "$d/gradlew" ]; then
              echo "== Gradle build in $d =="
              (cd "$d" && ./gradlew build -x test) || echo "WARN: Gradle build failed in $d (continuing)"
            fi
          done
          for w in $(find . -maxdepth 4 -name mvnw -type f); do
            d=$(dirname "$w")
            echo "== Maven build in $d =="
            (cd "$d" && chmod +x mvnw && ./mvnw -q -DskipTests package) || echo "WARN: Maven build failed in $d (continuing)"
          done
        '''
      }
    }
    stage('CodeQL database + analyze (Java + JS)') {
      steps {
        sh '''
          set -eux
          export PATH="${NODE_DIR}/bin:$PATH"
          rm -rf codeql-db-java codeql-db-js codeql-*.sarif
          
          cat > codeql-build-java.sh << 'EOF'
#!/bin/bash
set -eux
for dir in auth gateway registry user; do
  if [ -x "${dir}/gradlew" ]; then
    echo "== CodeQL trace Gradle in ${dir} =="
    (cd "${dir}" && ./gradlew --no-daemon clean compileJava)
  fi
done
EOF
          chmod +x codeql-build-java.sh
          
          echo "=== Creating Java database ==="
          "${CODEQL_DIR}/codeql" database create codeql-db-java \
            --language=java \
            --source-root . \
            --command=./codeql-build-java.sh
          
          echo "=== Analyzing Java with ${CODEQL_RAM}MB RAM ==="
          "${CODEQL_DIR}/codeql" database analyze codeql-db-java \
            codeql/java-queries:codeql-suites/java-security-and-quality.qls \
            --format=sarifv2.1.0 \
            --output=codeql-java.sarif \
            --ram=${CODEQL_RAM} \
            --threads=0
          
          echo "=== Java analysis complete ==="
          ls -lh codeql-java.sarif
          
          echo "=== Creating JavaScript/TypeScript database ==="
          "${CODEQL_DIR}/codeql" database create codeql-db-js \
            --language=javascript \
            --source-root .
          
          echo "=== Analyzing JavaScript/TypeScript with ${CODEQL_RAM}MB RAM ==="
          "${CODEQL_DIR}/codeql" database analyze codeql-db-js \
            codeql/javascript-queries:codeql-suites/javascript-security-and-quality.qls \
            --format=sarifv2.1.0 \
            --output=codeql-js.sarif \
            --ram=${CODEQL_RAM} \
            --threads=0
          
          echo "=== JavaScript/TypeScript analysis complete ==="
          ls -lh codeql-js.sarif
          
          echo "=== All analyses complete ==="
          ls -lh codeql-*.sarif
        '''
      }
    }
    stage('Quality Gate CodeQL') {
      steps {
        script {
          sh '''
            set -eux
            
            cat > analyze_sarif.py << 'PYTHON_EOF'
#!/usr/bin/env python3
import json
import sys
import os

def analyze_sarif_file(filepath):
    with open(filepath, 'r') as f:
        data = json.load(f)
    
    severity_counts = {
        'critical': 0,
        'high': 0,
        'medium': 0,
        'low': 0,
        'note': 0,
        'unknown': 0
    }
    
    for run in data.get('runs', []):
        for result in run.get('results', []):
            level = result.get('level', 'note').lower()
            
            if level == 'error':
                tags = []
                for rule in run.get('tool', {}).get('driver', {}).get('rules', []):
                    if rule.get('id') == result.get('ruleId'):
                        tags = rule.get('properties', {}).get('tags', [])
                        break
                
                if 'security' in tags and any(t in tags for t in ['external/cwe/cwe-89', 'external/cwe/cwe-79', 'external/cwe/cwe-78']):
                    severity_counts['critical'] += 1
                else:
                    severity_counts['high'] += 1
            elif level == 'warning':
                severity_counts['medium'] += 1
            elif level == 'note':
                severity_counts['low'] += 1
            else:
                severity_counts['unknown'] += 1
    
    return severity_counts

def main():
    max_critical = int(os.environ.get('MAX_CRITICAL_ISSUES', '0'))
    max_high = int(os.environ.get('MAX_HIGH_ISSUES', '5'))
    max_medium = int(os.environ.get('MAX_MEDIUM_ISSUES', '20'))
    
    total_counts = {
        'critical': 0,
        'high': 0,
        'medium': 0,
        'low': 0,
        'note': 0,
        'unknown': 0
    }
    
    sarif_files = ['codeql-java.sarif', 'codeql-js.sarif']
    
    for sarif_file in sarif_files:
        if os.path.exists(sarif_file):
            print(f"\\n=== Analyzing {sarif_file} ===")
            counts = analyze_sarif_file(sarif_file)
            
            for severity, count in counts.items():
                total_counts[severity] += count
                if count > 0:
                    print(f"  {severity.upper()}: {count}")
        else:
            print(f"WARNING: {sarif_file} not found")
    
    print("\\n" + "="*60)
    print("CODEQL SECURITY ANALYSIS SUMMARY")
    print("="*60)
    print(f"  CRITICAL: {total_counts['critical']}")
    print(f"  HIGH:     {total_counts['high']}")
    print(f"  MEDIUM:   {total_counts['medium']}")
    print(f"  LOW:      {total_counts['low']}")
    print(f"  TOTAL:    {sum(total_counts.values())}")
    print("="*60)
    
    failures = []
    
    if total_counts['critical'] > max_critical:
        failures.append(f"CRITICAL issues: {total_counts['critical']} (max: {max_critical})")
    
    if total_counts['high'] > max_high:
        failures.append(f"HIGH issues: {total_counts['high']} (max: {max_high})")
    
    if total_counts['medium'] > max_medium:
        failures.append(f"MEDIUM issues: {total_counts['medium']} (max: {max_medium})")
    
    branch_name = os.environ.get('BRANCH_NAME', '')
    is_main_branch = branch_name in ['main', 'master', 'production']
    
    if failures:
        print("\\n" + "!"*60)
        print("QUALITY GATE FAILED")
        print("!"*60)
        for failure in failures:
            print(f"  ✗ {failure}")
        print("!"*60)
        
        if is_main_branch:
            print(f"\\n⛔ Build BLOCKED on branch '{branch_name}' due to quality gate failure")
            sys.exit(1)
        else:
            print(f"\\n⚠️  Warning on branch '{branch_name}' - Quality gate would fail on main branch")
            print("Fix these issues before merging to main/master/production")
            sys.exit(0)
    else:
        print("\\n" + "✓"*60)
        print("QUALITY GATE PASSED")
        print("✓"*60)
        sys.exit(0)

if __name__ == '__main__':
    main()
PYTHON_EOF

            chmod +x analyze_sarif.py
            python3 analyze_sarif.py
          '''
        }
      }
    }
    stage('Archive SARIF') {
      steps {
        archiveArtifacts artifacts: 'codeql-*.sarif', fingerprint: true
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: 'codeql.zip, codeql-*.sarif', allowEmptyArchive: true
    }
    success {
      echo "✅ Pipeline completed successfully - CodeQL quality gate passed"
    }
    failure {
      echo "❌ Pipeline failed - Check CodeQL quality gate results above and fix issues before merging to main/master/production"
    }
  }
}

stage('Deploy to TAF (A - minimal)') {
  when { branch 'main' }
  steps {
    sh '''
      set -eux
      TAF_HOST="172.31.21.242"
      TAF_USER="ubuntu"

      mkdir -p /var/jenkins_home/.ssh
      chmod 700 /var/jenkins_home/.ssh
      ssh-keyscan -H "${TAF_HOST}" >> /var/jenkins_home/.ssh/known_hosts

      ssh ${TAF_USER}@${TAF_HOST} "
        set -eux
        cd ~/TAF-FALL-2025
        git pull
        if [ -f docker-compose-local-test.yml ]; then
          docker compose -f docker-compose-local-test.yml up -d --build
        else
          docker compose up -d --build
        fi
      "
    '''
  }
}
