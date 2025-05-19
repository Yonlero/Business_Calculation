INSERT INTO apportionments (id, account, cost_center, business_unit, year_month, origin_id, is_origin)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'AC-0001', 'CC-0001', 'BU-0001', 202501, NULL, TRUE),
       ('550e8400-e29b-41d4-a716-446655440001', 'AC-0002', 'CC-0002', 'BU-0002', 202501,
        '550e8400-e29b-41d4-a716-446655440000', FALSE),
       ('550e8400-e29b-41d4-a716-446655440002', 'AC-0003', 'CC-0003', 'BU-0003', 202501,
        '550e8400-e29b-41d4-a716-446655440000', FALSE),
       ('550e8400-e29b-41d4-a716-446655440003', 'AC-0004', 'CC-0004', 'BU-0004', 202501,
        '550e8400-e29b-41d4-a716-446655440000', FALSE),
       ('550e8400-e29b-41d4-a716-446655440004', 'AC-0005', 'CC-0005', 'BU-0005', 202501,
        '550e8400-e29b-41d4-a716-446655440000', FALSE);

INSERT INTO value_distributions (id, apportionment_id, month, percentage, destination_id)
VALUES (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', 1, 0.10, '550e8400-e29b-41d4-a716-446655440001'),
       (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', 1, 0.20, '550e8400-e29b-41d4-a716-446655440002'),
       (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', 1, 0.15, '550e8400-e29b-41d4-a716-446655440003'),
       (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', 1, 0.10, '550e8400-e29b-41d4-a716-446655440004');