import { LoginRequestDTO } from '../dto/request/LoginRequestDTO';
import { LoginResponseDTO } from '../dto/response/LoginResponseDTO';
import { RegisterRequestDTO } from '../dto/request/RegisterRequestDTO';
import { RegisterResponseDTO } from '../dto/response/RegisterResponseDTO';
import { post } from './httpClient';
import { API_BASE_URL } from '../config';

export async function login(request: LoginRequestDTO): Promise<LoginResponseDTO> {
  const response = await post<LoginRequestDTO, LoginResponseDTO>(
    `${API_BASE_URL}/auth/login`,
    request,
    false
  );

  if (!response.success || !response.data) {
    throw new Error(response.error?.message || 'Login failed');
  }

  return response.data;
}

export async function register(request: RegisterRequestDTO): Promise<RegisterResponseDTO> {
  const response = await post<RegisterRequestDTO, RegisterResponseDTO>(
    `${API_BASE_URL}/auth/register`,
    request,
    false
  );

  if (!response.success || !response.data) {
    throw new Error(response.error?.message || 'Registration failed');
  }

  return response.data;
}

export async function logout(keepalive = false): Promise<void> {
  const token = localStorage.getItem('auth_token');
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  await fetch(`${API_BASE_URL}/auth/logout`, {
    method: 'POST',
    headers,
    body: '{}',
    keepalive,
  });
  localStorage.removeItem('auth_token');
  localStorage.removeItem('user_id');
  localStorage.removeItem('username');
}
