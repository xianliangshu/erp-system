import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { getUserMenus, type Menu } from '@/api/system/menu'

interface MenuState {
    menus: Menu[]
    loading: boolean
    setMenus: (menus: Menu[]) => void
    loadUserMenus: (userId: number) => Promise<void>
    clearMenus: () => void
}

export const useMenuStore = create<MenuState>()(
    persist(
        (set) => ({
            menus: [],
            loading: false,
            setMenus: (menus) => set({ menus }),
            loadUserMenus: async (userId: number) => {
                set({ loading: true })
                try {
                    console.log('🔍 Loading user menus for userId:', userId)
                    const response = await getUserMenus(userId)
                    console.log('🔍 API response:', response)
                    // response 已经是菜单数组,因为 request.ts 拦截器已经提取了 data.data
                    set({ menus: response || [], loading: false })
                    console.log('🔍 Menus saved to store:', response)
                } catch (error) {
                    console.error('Failed to load user menus:', error)
                    set({ menus: [], loading: false })
                }
            },
            clearMenus: () => set({ menus: [], loading: false })
        }),
        {
            name: 'menu-storage'
        }
    )
)
