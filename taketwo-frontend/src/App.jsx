import { useState } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import Navbar from './components/Navbar.jsx';
import SplashScreen from './components/SplashScreen.jsx';
import PageTransition from './components/PageTransition.jsx';

import DiscoverPage from './pages/DiscoverPage.jsx';
import MovieDetailsPage from './pages/MovieDetailsPage.jsx';
import DiaryPage from './pages/DiaryPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import ListsPage from './pages/ListsPage.jsx';
import MembersPage from './pages/MembersPage.jsx';
import ActivityPage from './pages/ActivityPage.jsx';
import TakeTwoPage from './pages/TakeTwoPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import InboxPage from './pages/InboxPage.jsx';
import ConversationPage from './pages/ConversationPage.jsx';
import DiscussionRoomPage from './pages/DiscussionRoomPage.jsx';
import DiscussionPostPage from './pages/DiscussionPostPage.jsx';

export default function App() {
  const [showSplash, setShowSplash] = useState(true);
  const location = useLocation(); // Track current route for smooth transitions

  return (
    <>
      <AnimatePresence>
        {showSplash && (
          <SplashScreen onFinish={() => {
            setTimeout(() => setShowSplash(false), 3500);
          }} />
        )}
      </AnimatePresence>

      <Navbar />
      
      {/* mode="wait" ensures the old page fades out cleanly before the new one glides in */}
      <AnimatePresence mode="wait">
        <Routes location={location} key={location.pathname}>
          <Route path="/" element={<PageTransition><DiscoverPage /></PageTransition>} />
          <Route path="/movie/:tmdbId" element={<PageTransition><MovieDetailsPage /></PageTransition>} />
          <Route path="/user/:userId" element={<PageTransition><ProfilePage /></PageTransition>} />
          <Route 
            path="/profile" 
            element={
              <ProtectedRoute>
                <PageTransition><ProfilePage /></PageTransition>
              </ProtectedRoute>
            } 
          />
          <Route path="/login" element={<PageTransition><LoginPage /></PageTransition>} />
          <Route path="/register" element={<PageTransition><RegisterPage /></PageTransition>} />
          <Route
            path="/activity"
            element={
              <ProtectedRoute>
                <PageTransition><ActivityPage /></PageTransition>
              </ProtectedRoute>
            }
          />
          <Route
            path="/members"
            element={
              <ProtectedRoute>
                <PageTransition><MembersPage /></PageTransition>
              </ProtectedRoute>
            }
          />
          <Route
            path="/diary"
            element={
              <ProtectedRoute>
                <PageTransition><DiaryPage /></PageTransition>
              </ProtectedRoute>
            }
          />
          <Route
            path="/lists"
            element={
              <ProtectedRoute>
                <PageTransition><ListsPage /></PageTransition>
              </ProtectedRoute>
            }
          />
          <Route
            path="/take-two"
            element={
              <ProtectedRoute>
                <PageTransition><TakeTwoPage /></PageTransition>
              </ProtectedRoute>
            }
          />
          <Route 
            path="/messages" 
            element={
              <ProtectedRoute>
                <PageTransition><InboxPage /></PageTransition>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/messages/:conversationId" 
            element={
              <ProtectedRoute>
                <PageTransition><ConversationPage /></PageTransition>
              </ProtectedRoute>
            } 
          />
          <Route path="/discussions/movie/:tmdbId" element={<PageTransition><DiscussionRoomPage /></PageTransition>} />
          <Route path="/discussions/post/:postId" element={<PageTransition><DiscussionPostPage /></PageTransition>} />
        </Routes>
      </AnimatePresence>
    </>
  );
}