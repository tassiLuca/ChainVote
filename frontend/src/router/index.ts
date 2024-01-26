import {createRouter, createWebHistory} from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import NotificationsView from '@/views/NotificationsView.vue'
import VotingDetails from '@/views/VotingDetails.vue'
import NotFound from "@/views/NotFound.vue";
import Test from "@/views/Test.vue";
import axios from "axios";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
    },
    // {
    //   path: '/elections',
    //   name: 'elections',
    //   component: () => import('@/views/ElectionsTestView.vue'),
    // },
    {
      path: '/vote/:id',
      name: 'vote',
      beforeEnter: (to, from, next) => {
        try {
          axios.get(`http://localhost:8080/election/info/detail/${to.params.id}`)
            .then((response) => {
              to.meta.data = response.data.data;
              next();
            });
        } catch (error) {
          console.log(error);
          // next({ name: 'not-found' });
        }
      },
      component: () => import('@/views/VoteView.vue'),
    },
    {
      path: '/elections',
      name: 'elections',
      props: route => ({ qualifier: route.query.qualifier }),
      component: () => import('@/views/ElectionsView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/user/notifications',
      name: 'notifications',
      component: NotificationsView
    },
    {
      path: '/voting/details/:id',
      name: 'voting-details',
      component: VotingDetails
    },
    {
      path: '/user',
      name: 'user-area',
      component: () => import('@/views/UserAreaView.vue'),
    },
    {
      path: '/register', 
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/elections/create',
      name: 'create-election',
      component: () => import('@/views/CreateElectionView.vue'),
    },
    {
      path: '/test',
      name: 'test',
      component: Test,
    },
    {
      // TODO: change path
      path: '/error',
      name: 'error',
      component: () => import('@/views/ErrorView.vue'),
    },
    {
      // TODO: change path
      path: '/no-permission',
      name: 'no-permission',
      component: () => import('@/views/NoPermissionView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFound,
    },
  ]
})

export default router
