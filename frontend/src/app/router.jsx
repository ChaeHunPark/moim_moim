import { createBrowserRouter } from "react-router-dom"
import Layout from "../components/common/Layout"
import HomePage from "../pages/Home/HomePage"

const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
    ],
  },
])

export default router
