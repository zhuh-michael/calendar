// Centralized audio manager (moved from kid app)
class AudioManager {
  constructor() {
    this.audioContext = null
    this.isEnabled = true
  }

  init() {
    try {
      this.audioContext = new (window.AudioContext || window.webkitAudioContext)()
    } catch (e) {
      console.warn('Web Audio API not supported')
      this.isEnabled = false
    }
  }

  playSuccessSound() {
    if (!this.isEnabled || !this.audioContext) return
    try {
      const notes = [523.25, 659.25, 783.99]
      notes.forEach((frequency, index) => {
        setTimeout(() => {
          this.playTone(frequency, 0.3, 'sine')
        }, index * 150)
      })
    } catch (e) {
      console.warn('Failed to play success sound')
    }
  }

  playCoinSound() {
    if (!this.isEnabled || !this.audioContext) return
    try {
      const notes = [800, 1000, 1200]
      notes.forEach((frequency, index) => {
        setTimeout(() => {
          this.playTone(frequency, 0.2, 'sine')
        }, index * 100)
      })
    } catch (e) {
      console.warn('Failed to play coin sound')
    }
  }

  playGachaSound() {
    if (!this.isEnabled || !this.audioContext) return
    try {
      let angle = 0
      const interval = setInterval(() => {
        const frequency = 400 + Math.sin(angle) * 200
        this.playTone(frequency, 0.1, 'square')
        angle += 0.5
        if (angle > Math.PI * 4) {
          clearInterval(interval)
        }
      }, 100)
    } catch (e) {
      console.warn('Failed to play gacha sound')
    }
  }

  playTone(frequency, duration, waveType = 'sine') {
    if (!this.audioContext) return
    try {
      const oscillator = this.audioContext.createOscillator()
      const gainNode = this.audioContext.createGain()
      oscillator.connect(gainNode)
      gainNode.connect(this.audioContext.destination)
      oscillator.frequency.setValueAtTime(frequency, this.audioContext.currentTime)
      oscillator.type = waveType
      gainNode.gain.setValueAtTime(0.3, this.audioContext.currentTime)
      gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + duration)
      oscillator.start(this.audioContext.currentTime)
      oscillator.stop(this.audioContext.currentTime + duration)
    } catch (e) {
      console.warn('Failed to play tone')
    }
  }

  setEnabled(enabled) {
    this.isEnabled = enabled
  }
}

const audioManager = new AudioManager()
export const initAudio = () => { audioManager.init() }
export const playSuccessSound = () => audioManager.playSuccessSound()
export const playCoinSound = () => audioManager.playCoinSound()
export const playGachaSound = () => audioManager.playGachaSound()
export const setAudioEnabled = (enabled) => audioManager.setEnabled(enabled)
export default audioManager


