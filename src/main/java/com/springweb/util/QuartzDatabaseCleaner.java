package com.springweb.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Utility to diagnose and clean up corrupt Quartz triggers
 * Use this when you see BYTEA conversion errors
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public void diagnoseTriggers() {
        log.info("========== QUARTZ TRIGGER DIAGNOSIS ==========");
        
        String query = """
            SELECT 
                t.trigger_name,
                t.trigger_type,
                t.trigger_group,
                CASE 
                    WHEN EXISTS (SELECT 1 FROM qrtz_simple_triggers st 
                                 WHERE st.trigger_name = t.trigger_name 
                                 AND st.trigger_group = t.trigger_group) THEN 'SIMPLE'
                    WHEN EXISTS (SELECT 1 FROM qrtz_cron_triggers ct 
                                 WHERE ct.trigger_name = t.trigger_name 
                                 AND ct.trigger_group = t.trigger_group) THEN 'CRON'
                    WHEN EXISTS (SELECT 1 FROM qrtz_blob_triggers bt 
                                 WHERE bt.trigger_name = t.trigger_name 
                                 AND bt.trigger_group = t.trigger_group) THEN 'BLOB'
                    ELSE 'ORPHANED'
                END as actual_storage
            FROM qrtz_triggers t
            ORDER BY t.trigger_name
            """;
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            
            if (results.isEmpty()) {
                log.info("No triggers found in database");
                return;
            }
            
            log.info("Found {} triggers:", results.size());
            int mismatchCount = 0;
            
            for (Map<String, Object> row : results) {
                String triggerName = (String) row.get("trigger_name");
                String triggerType = (String) row.get("trigger_type");
                String triggerGroup = (String) row.get("trigger_group");
                String actualStorage = (String) row.get("actual_storage");
                
                boolean isMismatch = !triggerType.equals(actualStorage);
                
                if (isMismatch) {
                    log.error("MISMATCH: {} / {} - Type: {} but stored in: {}", 
                        triggerGroup, triggerName, triggerType, actualStorage);
                    mismatchCount++;
                } else {
                    log.info("OK: {} / {} - Type: {} (correct)", 
                        triggerGroup, triggerName, triggerType);
                }
            }
            
            if (mismatchCount > 0) {
                log.error("Found {} triggers with type mismatches! Run cleanupPaymentTriggers()", mismatchCount);
            } else {
                log.info("All triggers are correctly stored");
            }
            
        } catch (Exception e) {
            log.error("Error diagnosing triggers", e);
        }
        
        log.info("===============================================");
    }

    public void cleanupPaymentTriggers() {
        log.info("Cleaning up payment triggers...");
        
        try {
            jdbcTemplate.update("DELETE FROM qrtz_fired_triggers WHERE trigger_group = 'paymentGroup'");
            jdbcTemplate.update("DELETE FROM qrtz_simple_triggers WHERE trigger_group = 'paymentGroup'");
            jdbcTemplate.update("DELETE FROM qrtz_cron_triggers WHERE trigger_group = 'paymentGroup'");
            jdbcTemplate.update("DELETE FROM qrtz_blob_triggers WHERE trigger_group = 'paymentGroup'");
            jdbcTemplate.update("DELETE FROM qrtz_triggers WHERE trigger_group = 'paymentGroup'");
            jdbcTemplate.update("DELETE FROM qrtz_job_details WHERE job_group = 'paymentGroup'");
            
            log.info("Payment triggers cleaned up successfully");
            
        } catch (Exception e) {
            log.error("Error cleaning up payment triggers", e);
            throw e;
        }
    }

    public void cleanupAllTriggers() {
        log.warn("Cleaning up ALL Quartz data!");
        
        try {
            jdbcTemplate.update("DELETE FROM qrtz_fired_triggers");
            jdbcTemplate.update("DELETE FROM qrtz_simple_triggers");
            jdbcTemplate.update("DELETE FROM qrtz_cron_triggers");
            jdbcTemplate.update("DELETE FROM qrtz_simprop_triggers");
            jdbcTemplate.update("DELETE FROM qrtz_blob_triggers");
            jdbcTemplate.update("DELETE FROM qrtz_triggers");
            jdbcTemplate.update("DELETE FROM qrtz_job_details");
            
            log.info("All Quartz data cleaned up");
            
        } catch (Exception e) {
            log.error("Error cleaning up all triggers", e);
            throw e;
        }
    }
}
