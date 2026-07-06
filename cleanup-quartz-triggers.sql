-- Quartz Database Cleanup Script
-- Run this if you continue to see "Bad value for type long" errors

-- Step 1: Check for problematic triggers
SELECT 
    t.trigger_name, 
    t.trigger_type, 
    t.trigger_group,
    CASE 
        WHEN t.trigger_type = 'SIMPLE' AND st.trigger_name IS NULL THEN 'MISSING SIMPLE DATA'
        WHEN t.trigger_type = 'CRON' AND ct.trigger_name IS NULL THEN 'MISSING CRON DATA'
        ELSE 'OK'
    END as status
FROM qrtz_triggers t
LEFT JOIN qrtz_simple_triggers st 
    ON t.trigger_name = st.trigger_name 
    AND t.trigger_group = st.trigger_group
LEFT JOIN qrtz_cron_triggers ct 
    ON t.trigger_name = ct.trigger_name 
    AND t.trigger_group = ct.trigger_group
WHERE t.trigger_group = 'paymentGroup';

-- Step 2: Find triggers with NULL or corrupt data in simple_triggers
SELECT * FROM qrtz_simple_triggers 
WHERE repeat_count IS NULL 
   OR times_triggered IS NULL
   OR repeat_count = '';

-- Step 3: Clean up corrupt payment triggers (CAUTION: This deletes data!)
-- Uncomment the lines below to execute cleanup

-- DELETE FROM qrtz_fired_triggers WHERE trigger_group = 'paymentGroup';
-- DELETE FROM qrtz_simple_triggers WHERE trigger_group = 'paymentGroup';
-- DELETE FROM qrtz_cron_triggers WHERE trigger_group = 'paymentGroup';
-- DELETE FROM qrtz_triggers WHERE trigger_group = 'paymentGroup';

-- Step 4: Full cleanup (if needed - deletes ALL Quartz data)
-- USE WITH EXTREME CAUTION - Only if all triggers are corrupt

-- DELETE FROM qrtz_fired_triggers;
-- DELETE FROM qrtz_simple_triggers;
-- DELETE FROM qrtz_cron_triggers;
-- DELETE FROM qrtz_simprop_triggers;
-- DELETE FROM qrtz_blob_triggers;
-- DELETE FROM qrtz_triggers;
-- DELETE FROM qrtz_job_details;

-- Step 5: Verify cleanup
SELECT COUNT(*) as trigger_count FROM qrtz_triggers;
SELECT COUNT(*) as job_count FROM qrtz_job_details;
