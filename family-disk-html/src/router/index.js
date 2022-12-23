import { createRouter } from "vue-router";
import { createWebHashHistory } from "vue-router";

import User from "@/views/User.vue";
import Disk from "@/views/Disk.vue";
import Notepad from "@/views/Notepad.vue";
import Journal from "@/views/Journal.vue";

const routes = [
    { path: '/user', component: User },
    { path: '/disk', component: Disk },
    { path: '/notepad', component: Notepad },
    { path: '/journal', component: Journal }
]
const router = createRouter({
    // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: createWebHashHistory(),
    routes, // `routes: routes` 的缩写
});

export default router;