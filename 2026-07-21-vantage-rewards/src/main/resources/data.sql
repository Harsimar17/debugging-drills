-- Seed members (identity ids assigned in insertion order: 1, 2, 3, 4)
INSERT INTO member (member_number, full_name, email, tier, status, enrolled_at, version)
VALUES ('VG100001', 'Ava Thompson',   'ava.thompson@example.com',   'STANDARD', 'ACTIVE', TIMESTAMP '2025-02-11 09:30:00', 0);
INSERT INTO member (member_number, full_name, email, tier, status, enrolled_at, version)
VALUES ('VG100002', 'Bruno Adeyemi',  'bruno.adeyemi@example.com',  'GOLD',     'ACTIVE', TIMESTAMP '2024-11-03 14:05:00', 0);
INSERT INTO member (member_number, full_name, email, tier, status, enrolled_at, version)
VALUES ('VG100003', 'Chandni Rao',    'chandni.rao@example.com',    'PLATINUM', 'ACTIVE', TIMESTAMP '2024-08-19 18:45:00', 0);
INSERT INTO member (member_number, full_name, email, tier, status, enrolled_at, version)
VALUES ('VG100004', 'Diego Fuentes',  'diego.fuentes@example.com',  'SILVER',   'ACTIVE', TIMESTAMP '2025-05-27 11:20:00', 0);

-- Reward catalog
INSERT INTO reward_catalog_item (sku, name, points_cost, active) VALUES ('COFFEE-01', 'Free Coffee',          150,  TRUE);
INSERT INTO reward_catalog_item (sku, name, points_cost, active) VALUES ('VOUCHER-10', '$10 Store Voucher',   900,  TRUE);
INSERT INTO reward_catalog_item (sku, name, points_cost, active) VALUES ('VOUCHER-25', '$25 Store Voucher',   2100, TRUE);
INSERT INTO reward_catalog_item (sku, name, points_cost, active) VALUES ('HEADPHONES', 'Wireless Headphones', 12000, TRUE);
INSERT INTO reward_catalog_item (sku, name, points_cost, active) VALUES ('LEGACY-05',  'Discontinued Reward', 500,  FALSE);

-- Historical ledger for Ava (member 1): earned points, some already spent
INSERT INTO points_ledger_entry (member_id, entry_type, points, description, source_reference, earned_at, expires_at, expired)
VALUES (1, 'EARN',   1200, 'Points earned (base)', 'ORDER-8801', TIMESTAMP '2025-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00', FALSE);
INSERT INTO points_ledger_entry (member_id, entry_type, points, description, source_reference, earned_at, expires_at, expired)
VALUES (1, 'EARN',    350, 'Points earned (base)', 'ORDER-8830', TIMESTAMP '2025-06-14 16:30:00', TIMESTAMP '2026-06-14 16:30:00', FALSE);
INSERT INTO points_ledger_entry (member_id, entry_type, points, description, source_reference, earned_at, expires_at, expired)
VALUES (1, 'REDEEM', -900, 'Redeemed $10 Store Voucher', 'VOUCHER-10', TIMESTAMP '2025-07-02 12:15:00', NULL, FALSE);

-- Historical ledger for Bruno (member 2): gold tier earnings
INSERT INTO points_ledger_entry (member_id, entry_type, points, description, source_reference, earned_at, expires_at, expired)
VALUES (2, 'EARN', 2000, 'Points earned (base)',            'ORDER-9001', TIMESTAMP '2025-01-20 09:00:00', TIMESTAMP '2026-01-20 09:00:00', FALSE);
INSERT INTO points_ledger_entry (member_id, entry_type, points, description, source_reference, earned_at, expires_at, expired)
VALUES (2, 'EARN', 1000, 'Points earned (GOLD tier bonus)', 'ORDER-9001', TIMESTAMP '2025-01-20 09:00:00', TIMESTAMP '2026-01-20 09:00:00', FALSE);
