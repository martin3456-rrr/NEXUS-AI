import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import axios from 'axios';

const Dashboard = () => {
    const [metrics, setMetrics] = useState({ aiPredictions: [], services: [] });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMetrics = async () => {
            try {
                const [aiRes, servicesRes] = await Promise.all([
                    axios.get('/api/ai/metrics', { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } }),
                    axios.get('/api/health')
                ]);
                setMetrics({ aiPredictions: aiRes.data, services: servicesRes.data });
            } catch (error) {
                console.error('Błąd pobierania metryk:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchMetrics();
        const interval = setInterval(fetchMetrics, 30000);
        return () => clearInterval(interval);
    }, []);

    if (loading) return <div>Ładowanie dashboardu...</div>;

    return (
        <div className="dashboard">
            <h1>NEXUS-AI Dashboard</h1>

            <section>
                <h2>Predykcje AI (LSTM Model)</h2>
                <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={metrics.aiPredictions}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="timestamp" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="prediction" stroke="#8884d8" />
                        <Line type="monotone" dataKey="actual" stroke="#82ca9d" />
                    </LineChart>
                </ResponsiveContainer>
            </section>
            <section>
                <h2>Status Serwisów</h2>
                <ul>
                    {metrics.services.map(service => (
                        <li key={service.name}>
                            {service.name}: {service.status} (Uptime: {service.uptime}%)
                        </li>
                    ))}
                </ul>
            </section>
            <section>
                <h2>Blockchain Status</h2>
                <p>Walidator: {metrics.blockchain?.currentValidator}</p>
                <p>Bloki: {metrics.blockchain?.blockCount}</p>
            </section>
        </div>
    );
};

export default Dashboard;
