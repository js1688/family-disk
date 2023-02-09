import { createRouter } from "vue-router";
import { createWebHashHistory } from "vue-router";

import Formal from "@/views/formal/Formal.vue";
import User from "@/views/formal/User.vue";
import Disk from "@/views/formal/Disk.vue";
import Notepad from "@/views/formal/Notepad.vue";
import Journal from "@/views/formal/Journal.vue";
import NotepadShare from "@/views/share/NotepadShare.vue";
import Share from "@/views/share/Share.vue";
import HTTP404 from "@/views/public/Http404.vue";

import {key} from "@/global/KeyGlobal";
const routes = [
    {
        path: '/',
        //路由默认路径,看是否已登录
        redirect: localStorage.getItem(key().authorization) == null ? '/formal/user' : '/formal/disk'
    },
    {
        //正式功能路由
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
        //分享展示面板路由
        path: '/share',
        component: Share,
        children: [
            {
                path: 'notepad',
                component: NotepadShare,
            }
        ]
    },
    {
        //404
        path: '/404',
        component: HTTP404
    },
    {
        path: '/:pathMatch(.*)',
        redirect: '/404',
        hidden: true
    }
]
const router = createRouter({
    // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: createWebHashHistory(),
    routes, // `routes: routes` 的缩写
});

export default router;