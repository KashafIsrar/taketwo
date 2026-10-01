import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import DiscoverPage from './pages/DiscoverPage.jsx';
import MovieDetailsPage from './pages/MovieDetailsPage.jsx';
import DiaryPage from './pages/DiaryPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import ListsPage from './pages/ListsPage.jsx';
import MembersPage from './pages/MembersPage.jsx';
import ActivityPage from './pages/ActivityPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<DiscoverPage />} />
        <Route path="/movie/:tmdbId" element={<MovieDetailsPage />} />
        <Route path="/user/:userId" element={<ProfilePage />} />
        <Route 
          path="/profile" 
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          } 
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/activity"
          element={
            <ProtectedRoute>
              <ActivityPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/members"
          element={
            <ProtectedRoute>
              <MembersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/diary"
          element={
            <ProtectedRoute>
              <DiaryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/lists"
          element={
            <ProtectedRoute>
              <ListsPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </>
  );
}