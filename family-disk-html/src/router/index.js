import { createRouter } from "vue-router";
import { createWebHashHistory } from "vue-router";

import Formal from "@/views/formal/Formal.vue";
import User from "@/views/formal/User.vue";
import Disk from "@/views/formal/Disk.vue";
import Notepad from "@/views/formal/Notepad.vue";
import Journal from "@/views/formal/Journal.vue";
import NotepadShare from "@/views/share/NotepadShare.vue";
import Share from "@/views/share/Share.vue";
import {key} from "@/global/KeyGlobal";
const routes = [
    {
        path: '/',
        //路由默认路径,看是否已登录
        redirect: localStorage.getItem(key().authorization) == null ? '/formal/user' : '/formal/disk'
    },
    {
        path: '/formal',
        component: Formal,
        children: [
            {
                path: 'user',
                component: User,
            },
            {
                path: 'disk',
                component: Disk,
            },
            {
                path: 'notepad',
                component: Notepad,
            },
            {
                path: 'journal',
                component: Journal,
            }
        ]
    },
    {
        path: '/share',
        component: Share,
        children: [
            {
                path: 'notepad',
                component: NotepadShare,
            }
        ]
    }
]
const router = createRouter({
    // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: createWebHashHistory(),
    routes, // `routes: routes` 的缩写
});

export default router;