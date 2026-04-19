import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import BookRidePage from './pages/BookRidePage';
import TrackRidePage from './pages/TrackRidePage';
import HistoryPage from './pages/HistoryPage';
export default function App() {
    return (<BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />}/>
        <Route path="/book" element={<BookRidePage />}/>
        <Route path="/track/:rideId" element={<TrackRidePage />}/>
        <Route path="/history" element={<HistoryPage />}/>
      </Routes>
    </BrowserRouter>);
}
