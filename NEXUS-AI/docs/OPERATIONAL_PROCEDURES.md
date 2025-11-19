## Rollback Procedure If deploying the new version of *payment-service* causes 500 errors:

Check the deployment history: *kubectl rollout history deployment/payment-service -n nexus-ai*

Rollback to the previous version: *kubectl rollout undo deployment/payment-service -n nexus-ai*

## Backup and Restore Procedure (PostgreSQL) Automatic backups are performed daily at 2:00 AM (CronJob).
## Manual backup:
```bash
kubectl exec -it (kubectl get pods -l app=postgres -o jsonpath="{.items[0].metadata.name}") -- pg_dump -U nexus user_db > backup_$(date +%F).sql
```
## Restore:
```bash
cat backup.sql | kubectl exec -i (POD_NAME) -- psql -U nexus user_db 
```