import React, { useState } from 'react';
import { AuthProvider, useAuth } from './AuthContext';

function Dashboard() {
    const { token, logout } = useAuth();
    const [metrics, setMetrics] = useState('0.5, 0.6, 0.7, 0.8, 0.9');
    const [prediction, setPrediction] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handlePredict = async () => {
        setIsLoading(true);
        setError('');
        setPrediction(null);

        try {
            const parsedMetrics = metrics.split(',').map(m => parseFloat(m.trim()));
            if (parsedMetrics.some(isNaN)) {
                throw new Error('Invalid input. Please provide comma-separated numbers.');
            }

            const response = await fetch('/api/analytics/predict', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ "input": parsedMetrics }),
            });

            if (!response.ok) {
                throw new Error(`Error from server: ${response.status} ${response.statusText}`);
            }

            const resultText = await response.text();
            setPrediction(resultText);

        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{
            fontFamily: 'Arial, sans-serif',
            maxWidth: '600px',
            margin: '50px auto',
            padding: '30px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            borderRadius: '10px',
            backgroundColor: '#f9f9f9',
            textAlign: 'center'
        }}>
            <button onClick={logout} style={{ float: 'right' }}>Logout</button>
            <h1 style={{ color: '#333' }}>AI Load Prediction Dashboard</h1>
            <p style={{ color: '#666' }}>Enter recent system metrics (comma-separated) to predict the next value.</p>

            <textarea
                value={metrics}
                onChange={(e) => setMetrics(e.target.value)}
                rows="4"
                style={{
                    width: '95%',
                    padding: '10px',
                    fontSize: '16px',
                    borderRadius: '5px',
                    border: '1px solid #ccc',
                    marginBottom: '20px'
                }}
                placeholder="e.g., 0.1, 0.2, 0.3, 0.4"
            />

            <button
                onClick={handlePredict}
                disabled={isLoading}
                style={{
                    padding: '12px 25px',
                    fontSize: '18px',
                    color: 'white',
                    backgroundColor: isLoading ? '#aaa' : '#007BFF',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer',
                    transition: 'background-color 0.3s'
                }}
            >
                {isLoading ? 'Predicting...' : 'Predict Load'}
            </button>

            {error && (
                <div style={{ marginTop: '20px', color: 'red', fontWeight: 'bold' }}>
                    Error: {error}
                </div>
            )}

            {prediction && (
                <div style={{
                    marginTop: '25px',
                    padding: '20px',
                    backgroundColor: '#eaf5ff',
                    borderRadius: '8px',
                    border: '1px solid #b8d9f3'
                }}>
                    <h2 style={{ color: '#0056b3', margin: '0 0 10px 0' }}>Prediction Result:</h2>
                    <p style={{ fontSize: '20px', fontWeight: 'bold', color: '#333' }}>{prediction}</p>
                </div>
            )}
        </div>
    );
}

function LoginScreen() {
    const { login } = useAuth();
    const [user, setUser] = useState('');
    const [pass, setPass] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        login(user, pass);
    };

    return (
        <form onSubmit={handleSubmit} style={{ textAlign: 'center', marginTop: '50px' }}>
            <h2>NEXUS AI Login</h2>
            <input placeholder="Username" value={user} onChange={e => setUser(e.target.value)} /><br />
            <input type="password" placeholder="Password" value={pass} onChange={e => setPass(e.target.value)} /><br />
            <button type="submit">Log in</button>
        </form>
    );
}

export default function App() {
    return (
        <AuthProvider>
            <AppContent />
        </AuthProvider>
    );
}

function AppContent() {
    const { token } = useAuth();
    return token ? <Dashboard /> : <LoginScreen />;
}
