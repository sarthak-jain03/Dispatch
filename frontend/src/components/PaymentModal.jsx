import React, { useState } from 'react';
import { createPaymentOrder, verifyPayment } from '../api/paymentApi';
import { CreditCard, ShieldCheck, Loader2 } from 'lucide-react';

export default function PaymentModal({ rideId, amount, onPaymentSuccess, onPaymentFailure }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handlePayment = async () => {
        setLoading(true);
        setError(null);
        try {
            // 1. Create Razorpay order on backend
            const orderData = await createPaymentOrder(rideId);
            
            // 2. Configure Razorpay checkout options
            const options = {
                key: orderData.razorpayKeyId,
                amount: orderData.amount,
                currency: orderData.currency,
                name: 'Dispatch Ride Booking',
                description: `Payment for Ride #${rideId.slice(0, 8)}`,
                order_id: orderData.orderId,
                handler: async function (response) {
                    setLoading(true);
                    try {
                        // 3. Verify signature on backend
                        const verificationPayload = {
                            rideId,
                            razorpayOrderId: response.razorpay_order_id,
                            razorpayPaymentId: response.razorpay_payment_id,
                            razorpaySignature: response.razorpay_signature,
                        };
                        const isVerified = await verifyPayment(verificationPayload);
                        if (isVerified) {
                            if (onPaymentSuccess) onPaymentSuccess();
                        } else {
                            throw new Error('Signature verification failed');
                        }
                    } catch (err) {
                        setError('Payment verification failed. Please contact support.');
                        if (onPaymentFailure) onPaymentFailure(err.message);
                    } finally {
                        setLoading(false);
                    }
                },
                prefill: {
                    name: 'Sarthak Jain',
                    email: 'sarthak@example.com',
                    contact: '9999999999'
                },
                theme: {
                    color: '#10B981' // Green theme matching Uber
                },
                modal: {
                    ondismiss: function () {
                        setLoading(false);
                    }
                }
            };

            const rzp = new window.Razorpay(options);
            rzp.on('payment.failed', function (response) {
                setError(`Payment failed: ${response.error.description}`);
                if (onPaymentFailure) onPaymentFailure(response.error.description);
                setLoading(false);
            });
            rzp.open();

        } catch (err) {
            setError(err?.response?.data?.message || err.message || 'Failed to initiate payment');
            setLoading(false);
        }
    };

    return (
        <div className="bg-uber-gray-800 border border-uber-gray-700 rounded-2xl p-6 shadow-2xl relative overflow-hidden animate-[fade-in_0.3s_ease-out]">
            {/* Ambient background glow */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />

            <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 flex items-center justify-center border border-emerald-500/20 shrink-0">
                    <CreditCard className="w-5 h-5 text-emerald-400" />
                </div>
                <div>
                    <h4 className="text-white font-bold text-lg">Secure Ride Payment</h4>
                    <p className="text-uber-gray-400 text-xs flex items-center gap-1 mt-0.5">
                        <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" /> Powered by Razorpay Secure
                    </p>
                </div>
            </div>

            <div className="bg-uber-gray-900/60 rounded-xl p-4 border border-uber-gray-800/80 mb-5">
                <div className="flex justify-between items-center mb-2">
                    <span className="text-uber-gray-400 text-sm">Ride ID</span>
                    <span className="text-white font-mono text-sm">#{rideId.slice(0, 8)}</span>
                </div>
                <div className="flex justify-between items-center border-t border-uber-gray-850 pt-2 mt-2">
                    <span className="text-uber-gray-400 text-sm font-semibold">Total Amount</span>
                    <span className="text-white text-xl font-bold font-mono">₹{amount.toFixed(2)}</span>
                </div>
            </div>

            {error && (
                <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-xl text-center font-medium">
                    ⚠️ {error}
                </div>
            )}

            <button
                onClick={handlePayment}
                disabled={loading}
                className="btn-green w-full py-3.5 text-base font-bold flex items-center justify-center gap-2 hover:scale-[1.01] active:scale-[0.99] transition-all duration-200"
            >
                {loading ? (
                    <>
                        <Loader2 className="w-5 h-5 animate-spin" />
                        Processing Securely...
                    </>
                ) : (
                    `Pay ₹${amount.toFixed(0)} Now`
                )}
            </button>
        </div>
    );
}
