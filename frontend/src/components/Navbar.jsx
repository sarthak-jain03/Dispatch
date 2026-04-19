import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Car, MapPin, Clock, Zap } from 'lucide-react';
const navLinks = [
    { to: '/', label: 'Home', icon: Zap },
    { to: '/book', label: 'Book Ride', icon: MapPin },
    { to: '/history', label: 'My Rides', icon: Clock },
];
export default function Navbar() {
    const location = useLocation();
    return (<nav className="fixed top-0 left-0 right-0 z-50 glass-strong">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2.5 no-underline">
            <div className="w-9 h-9 bg-white rounded-lg flex items-center justify-center">
              <Car className="w-5 h-5 text-black"/>
            </div>
            <span className="text-xl font-bold text-white tracking-tight">
              Dispatch
            </span>
          </Link>

          {/* Nav Links */}
          <div className="flex items-center gap-1">
            {navLinks.map(({ to, label, icon: Icon }) => {
            const isActive = location.pathname === to;
            return (<Link key={to} to={to} className={`
                    flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium
                    transition-all duration-200 no-underline
                    ${isActive
                    ? 'bg-white text-black'
                    : 'text-uber-gray-300 hover:text-white hover:bg-uber-gray-700'}
                  `}>
                  <Icon className="w-4 h-4"/>
                  <span className="hidden sm:inline">{label}</span>
                </Link>);
        })}
          </div>

          {/* User Avatar */}
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-uber-green to-uber-blue flex items-center justify-center text-sm font-bold text-white">
              U
            </div>
          </div>
        </div>
      </div>
    </nav>);
}
