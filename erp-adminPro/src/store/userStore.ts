import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User } from '@/types'

interface UserState {
    user: User | null
    token: string | null
    setUser: (user: User) => void
    setToken: (token: string) => void
    clearUser: () => void
}

export const useUserStore = create<UserState>()(
    persist(
        (set) => ({
            user: null,
            token: null,
            setUser: (user) => set({ user }),
            setToken: (token) => {
                localStorage.setItem('token', token)
                set({ token })
            },
            clearUser: () => {
                localStorage.removeItem('token')
                set({ user: null, token: null })
            }
        }),
        {
            name: 'user-storage'
        }
    )
)
