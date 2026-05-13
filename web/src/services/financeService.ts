import axios from "axios"

const API_URL = "https://audry-subsphenoidal-bovinely.ngrok-free.dev/api/finances"

export interface Movement {
  id: number
  type: "Ingreso" | "Egreso"
  title: string
  category: string
  amount: number
  date: string
}

export const getMovements = async () => {
  const response = await axios.get(
    `${API_URL}/movements`
  )

  return response.data
}

export const createMovement = async (
  movement: Omit<Movement, "id">
) => {
  const response = await axios.post(
    `${API_URL}/movements`,
    movement
  )

  return response.data
}

export const deleteMovement = async (
  id: number
) => {
  await axios.delete(
    `${API_URL}/movements/${id}`
  )
}