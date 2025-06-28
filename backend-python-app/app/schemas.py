from pydantic import BaseModel

class ChatMessage(BaseModel):
    """Single chat message with sender role."""
    role: str
    content: str
