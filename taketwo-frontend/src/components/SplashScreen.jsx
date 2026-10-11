import { motion } from 'framer-motion';

export default function SplashScreen({ onFinish }) {
  return (
    <motion.div
      initial={{ opacity: 1 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.6, ease: 'easeInOut' }}
      onAnimationComplete={onFinish}
      className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-[#0b0e11] text-white select-none"
    >
      <div className="text-center overflow-hidden py-4 px-6 flex flex-col items-center">
        
        {/* Minimalist Vector Clapperboard Loader */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="mb-6 relative w-12 h-10 border-2 border-[#89a] rounded-sm flex flex-col justify-between p-1 bg-[#14181c] shadow-lg"
        >
          {/* Clapperboard Top Hinge / Strip */}
          <motion.div 
            animate={{ rotate: [-8, 0, -8] }}
            transition={{ duration: 1.2, repeat: Infinity, ease: 'easeInOut' }}
            className="w-full h-2.5 bg-[#e06a2c] rounded-2xs origin-left flex justify-around items-center px-0.5"
          >
            <div className="w-1 h-1 bg-black rounded-full" />
            <div className="w-1 h-1 bg-black rounded-full" />
            <div className="w-1 h-1 bg-black rounded-full" />
          </motion.div>

          {/* Clapperboard Body lines */}
          <div className="flex flex-col gap-1 w-full opacity-60">
            <div className="h-0.5 bg-[#89a] w-full" />
            <div className="h-0.5 bg-[#89a] w-2/3" />
          </div>
        </motion.div>

        {/* Scaled-Down Clean Logo Collision */}
        <div className="flex items-center justify-center text-2xl md:text-3xl font-extrabold tracking-wider font-display mb-2">
          {/* TAKE slides in from left */}
          <motion.span
            initial={{ x: -60, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
            className="text-white mr-1.5 inline-block"
          >
            TAKE
          </motion.span>

          {/* TWO slides in from right */}
          <motion.span
            initial={{ x: 60, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
            style={{ color: '#e06a2c' }}
            className="inline-block"
          >
            TWO
          </motion.span>
        </div>
        
        {/* Cinematic Tagline */}
        <motion.p
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.3, ease: 'easeOut' }}
          className="text-[10px] md:text-xs text-[#89a] tracking-[0.3em] uppercase font-medium"
        >
          Your Cinematic Second Act
        </motion.p>
      </div>
    </motion.div>
  );
}