import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const errorRate = new Rate('errors');

export const options = {
    scenarios: {
        average_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 50 },
                { duration: '3m', target: 50 },
                { duration: '1m', target: 0 },
            ],
        },
        spike_test: {
            executor: 'ramping-vus',
            startTime: '5m',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 200 },
                { duration: '1m', target: 200 },
                { duration: '30s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<800'],
        errors: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.K6_TARGET_URL || 'http://localhost:8080';

export default function () {
    const loginPayload = JSON.stringify({ username: 'testuser', password: 'password123' });
    const params = { headers: { 'Content-Type': 'application/json' } };

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

    const loginSuccess = check(loginRes, { 'login 200': (r) => r.status === 200 });
    if (!loginSuccess) {
        errorRate.add(1);
        return;
    }

    const token = loginRes.json('token');
    const authParams = { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } };

    const aiPayload = JSON.stringify({ input: [0.5, 0.6, 0.7, 0.8, 0.9] });
    const aiRes = http.post(`${BASE_URL}/api/analytics/predict`, aiPayload, authParams);
    check(aiRes, { 'ai prediction 200': (r) => r.status === 200 });


    const paymentPayload = JSON.stringify({
        amount: 50.00,
        currency: "USD",
        description: "Load Test Payment",
        cardNumber: "4111111111111111",
        cardCvc: "123",
        cardExpiry: "12/30"
    });

    const payRes = http.post(`${BASE_URL}/api/payments/charge`, paymentPayload, authParams);
    check(payRes, { 'payment processed 200': (r) => r.status === 200 });

    sleep(1);
}