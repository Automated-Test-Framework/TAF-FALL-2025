# ca.etsmtl.taf
# ca.etsmtl.taf
## 🚀 Quick Start Commands

### First Time Build (30-45 minutes)
```powershell
.\start-taf-local.ps1 -Mode full -Build
```

### Subsequent Starts (2-3 minutes)
```powershell
.\start-taf-local.ps1 -Mode full
```

### Individual Team
```powershell
.\start-taf-local.ps1 -Mode team1 -Build   # Team 1 only
.\start-taf-local.ps1 -Mode team2 -Build   # Team 2 only
.\start-taf-local.ps1 -Mode team3 -Build   # Team 3 only
```

### Clean Up Everything
```powershell
.\start-taf-local.ps1 -Clean
```

## 🔍 Verification Steps

1. **Wait 2-3 minutes** after script completes
2. **Check status**:
   ```powershell
   docker compose -f docker-compose-local-test.yml ps
   ```
3. **Open browsers**:
   - http://localhost:4200 ← Team 1 UI
   - http://localhost:4300 ← Team 2 UI  
   - http://localhost:4400 ← Team 3 UI
