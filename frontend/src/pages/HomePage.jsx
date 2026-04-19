import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Zap, Shield, Clock, ArrowRight, Car, Users, } from 'lucide-react';
export default function HomePage() {
  return (<div className="flex flex-col min-h-screen pt-16">
    {/* ── Hero Section ── */}
    <section className="relative flex-1 flex items-center justify-center overflow-hidden px-4 py-20 sm:py-32">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        {/* Grid pattern */}
        <div className="absolute inset-0 opacity-[0.03]" style={{
          backgroundImage: 'linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)',
          backgroundSize: '60px 60px',
        }} />
        {/* Gradient orbs */}
        <div className="absolute top-1/4 -left-32 w-96 h-96 bg-uber-green/10 rounded-full blur-3xl animate-[float_6s_ease-in-out_infinite]" />
        <div className="absolute bottom-1/4 -right-32 w-96 h-96 bg-uber-blue/10 rounded-full blur-3xl animate-[float_8s_ease-in-out_infinite_2s]" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-uber-purple/5 rounded-full blur-3xl" />
      </div>

      <div className="relative z-10 text-center max-w-3xl mx-auto">


        {/* Heading */}
        <h1 className="text-5xl sm:text-7xl font-black text-white leading-[1.05] tracking-tight mb-6 animate-[slide-up_0.6s_ease-out]">
          Go anywhere with{' '}
          <span className="relative inline-block">
            <span className="bg-gradient-to-r from-uber-green via-uber-green-light to-uber-blue bg-clip-text text-transparent">
              Dispatch
            </span>
            <span className="absolute -bottom-1 left-0 right-0 h-1 bg-gradient-to-r from-uber-green to-uber-blue rounded-full opacity-50" />
          </span>
        </h1>

        {/* Sub */}
        <p className="text-lg sm:text-xl text-uber-gray-300 max-w-xl mx-auto mb-10 leading-relaxed animate-[slide-up_0.6s_ease-out_0.1s_both]">
          Request a ride, get matched with a driver in seconds. Real-time tracking from pickup to drop-off.
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 animate-[slide-up_0.6s_ease-out_0.2s_both]">
          <Link to="/book" className="btn-primary text-lg px-8 py-4 no-underline">
            <MapPin className="w-5 h-5" />
            Book a Ride
            <ArrowRight className="w-5 h-5" />
          </Link>
          <Link to="/history" className="btn-secondary text-base px-6 py-3.5 no-underline">
            <Clock className="w-4 h-4" />
            View Ride History
          </Link>
        </div>
      </div>
    </section>

    {/* ── Features Section ── */}
    <section className="relative px-4 py-20 border-t border-uber-gray-800">
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="text-3xl sm:text-4xl font-bold text-white mb-3">
            How it works
          </h2>
          <p className="text-uber-gray-400 text-base max-w-md mx-auto">
            A complete ride lifecycle — from booking to completion
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {[
            {
              icon: MapPin,
              title: 'Set Location',
              desc: 'Pick your pickup and drop-off on an interactive map',
              color: 'uber-green',
              gradient: 'from-uber-green/20 to-transparent',
            },
            {
              icon: Users,
              title: 'Match Driver',
              desc: 'Kafka + Redis find the optimal driver in real-time',
              color: 'uber-blue',
              gradient: 'from-uber-blue/20 to-transparent',
            },
            {
              icon: Car,
              title: 'Track Ride',
              desc: 'Live status updates as your driver approaches',
              color: 'uber-purple',
              gradient: 'from-uber-purple/20 to-transparent',
            },
            {
              icon: Shield,
              title: 'Arrive Safe',
              desc: 'Fare calculated with Haversine formula precision',
              color: 'uber-yellow',
              gradient: 'from-amber-500/20 to-transparent',
            },
          ].map((feat, i) => (<div key={feat.title} className="card group hover:!border-uber-gray-500 transition-all duration-300" style={{ animationDelay: `${i * 100}ms` }}>
            <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${feat.gradient} flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300`}>
              <feat.icon className={`w-6 h-6 text-${feat.color}`} />
            </div>
            <h3 className="text-white font-bold text-base mb-1.5">
              {feat.title}
            </h3>
            <p className="text-uber-gray-400 text-sm leading-relaxed">
              {feat.desc}
            </p>
          </div>))}
        </div>
      </div>
    </section>


    {/* ── Footer ── */}
    <footer className="border-t border-uber-gray-800 px-4 py-8">
      <div className="max-w-5xl mx-auto flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Car className="w-4 h-4 text-uber-gray-500" />
          <span className="text-sm text-uber-gray-500">Dispatch</span>
        </div>
        <span className="text-xs text-uber-gray-600">
          Spring Boot • Redis • Kafka • React
        </span>
      </div>
    </footer>
  </div>);
}
