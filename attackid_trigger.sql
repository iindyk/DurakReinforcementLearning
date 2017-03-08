CREATE TRIGGER befo_insert BEFORE INSERT ON AttackNets  
BEGIN  
SELECT CASE   
WHEN ((SELECT x FROM AttackNets DESC LIMIT 1 )< NEW.x)   
THEN NEW.attackid=(SELECT attackid FROM AttackNets DESC LIMIT 1)+1   
END;   
END;