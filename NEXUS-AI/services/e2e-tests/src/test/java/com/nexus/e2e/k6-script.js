import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
    },
};

const BASE_URL = __ENV.K6_TARGET_URL || 'http://localhost:8080';

export default function () {
    const payload = JSON.stringify({
        username: 'testuser',
        password: 'password123',
    });
    const params = { headers: { 'Content-Type': 'application/json' } };
    const loginRes = http.post(`${BASE_URL}/api/auth/login`, payload, params);

    check(loginRes, {
        'login status is 200': (r) => r.status === 200,
        'has token': (r) => r.json('token') !== undefined,
    });

    const token = loginRes.json('token');
    const authParams = { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } };
    const aiPayload = JSON.stringify({ input: [0.1, 0.2, 0.3] });
    const aiRes = http.post(`${BASE_URL}/api/analytics/predict`, aiPayload, authParams);

    check(aiRes, {
        'ai status is 200': (r) => r.status === 200,
        'prediction received': (r) => r.json('prediction') !== undefined,
    });
    sleep(1);
}