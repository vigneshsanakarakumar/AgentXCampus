import React, { useState } from 'react';
import api from '../../services/api';
import Card, { CardHeader } from './Card';
import Button from './Button';
import Badge from './Badge';
import { Bot, Send, Loader2, Wrench, CheckSquare, Layers, BookOpen, Sparkles, ShieldCheck } from 'lucide-react';

export const AgentXAssistant = ({ initialQuery = '', onActionCompleted }) => {
  const [query, setQuery] = useState(initialQuery);
  const [messages, setMessages] = useState([
    {
      sender: 'agent',
      text: "Hello! I am your AgentX Campus AI Assistant. How can I help you today?\n\nTry asking me about attendance regulations, hostel guidelines, class timetables, or campus inquiries!",
      steps: [],
    }
  ]);
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState('');

  const suggestions = [
    "My attendance is 68%. Help me fix it.",
    "I took leave on September 10 and September 17. Show my leave history.",
    "What is the minimum attendance requirement?",
    "Prepare me for my upcoming exam.",
    "What classes do I have today?",
    "The projector in CS-204 is not working."
  ];

  const handleSend = async (qToSend) => {
    const text = qToSend || query;
    if (!text.trim() || loading) return;

    const userMessage = { sender: 'user', text };
    setMessages((prev) => [...prev, userMessage]);
    setQuery('');
    setLoading(true);
    setCurrentStep('AgentX Assistant: Analyzing query and retrieving information...');

    const token = localStorage.getItem('token');
    let accumulatedText = '';
    let finalSteps = [];
    let detectedAgentType = 'AgentX Assistant';
    let actionData = null;
    let latencyMs = null;

    // Create placeholder agent response for live streaming
    setMessages((prev) => [
      ...prev,
      {
        sender: 'agent',
        text: '',
        agentType: 'AgentX Assistant',
        steps: [],
        streaming: true,
      }
    ]);

    try {
      const response = await fetch('/api/v1/agent/chat/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ query: text })
      });

      if (!response.ok || !response.body) {
        throw new Error('Streaming response not available');
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop(); // Retain incomplete line

        let currentEvent = null;

        for (const line of lines) {
          const trimmed = line.trim();
          if (trimmed.startsWith('event:')) {
            currentEvent = trimmed.substring(6).trim();
          } else if (trimmed.startsWith('data:')) {
            const dataStr = trimmed.substring(5).trim();
            if (!dataStr) continue;

            try {
              const parsed = JSON.parse(dataStr);

              if (currentEvent === 'step' || parsed.step) {
                const s = parsed.step;
                setCurrentStep(s);
                if (!finalSteps.includes(s)) {
                  finalSteps.push(s);
                }
                setMessages((prev) => {
                  const updated = [...prev];
                  const last = updated[updated.length - 1];
                  if (last && last.sender === 'agent') {
                    last.steps = [...finalSteps];
                  }
                  return updated;
                });
              } else if (currentEvent === 'token' || parsed.token) {
                accumulatedText += parsed.token;
                setMessages((prev) => {
                  const updated = [...prev];
                  const last = updated[updated.length - 1];
                  if (last && last.sender === 'agent') {
                    last.text = accumulatedText;
                    last.streaming = true;
                  }
                  return updated;
                });
              } else if (currentEvent === 'done' || parsed.answer) {
                detectedAgentType = parsed.agentType || detectedAgentType;
                actionData = parsed.actionData || null;
                latencyMs = parsed.latencyMs || null;
                const executionPlan = parsed.executionPlan || null;
                const verification = parsed.verification || null;
                if (parsed.steps && parsed.steps.length > 0) {
                  finalSteps = parsed.steps;
                }
                if (parsed.answer) {
                  accumulatedText = parsed.answer;
                }
                setMessages((prev) => {
                  const updated = [...prev];
                  const last = updated[updated.length - 1];
                  if (last && last.sender === 'agent') {
                    last.text = accumulatedText;
                    last.agentType = detectedAgentType;
                    last.actionData = actionData;
                    last.latencyMs = latencyMs;
                    last.steps = finalSteps;
                    last.executionPlan = executionPlan;
                    last.verification = verification;
                    last.streaming = false;
                  }
                  return updated;
                });
                if (onActionCompleted) {
                  onActionCompleted(parsed);
                }
              }
            } catch (pErr) {
              // Non-JSON SSE line
            }
          }
        }
      }

      setMessages((prev) => {
        const updated = [...prev];
        const last = updated[updated.length - 1];
        if (last && last.sender === 'agent') {
          last.streaming = false;
        }
        return updated;
      });

    } catch (err) {
      console.warn('Streaming error, falling back to standard chat API:', err);
      try {
        const res = await api.post('/agent/chat', { query: text });
        const data = res.data;
        setMessages((prev) => {
          const updated = [...prev];
          const last = updated[updated.length - 1];
          if (last && last.sender === 'agent') {
            last.text = data.answer;
            last.agentType = data.agentType;
            last.steps = data.steps || [];
            last.actionData = data.actionData;
            last.latencyMs = data.latencyMs;
            last.executionPlan = data.executionPlan || null;
            last.verification = data.verification || null;
            last.streaming = false;
          }
          return updated;
        });
        if (onActionCompleted) {
          onActionCompleted(data);
        }
      } catch (postErr) {
        setMessages((prev) => {
          const updated = [...prev];
          const last = updated[updated.length - 1];
          if (last && last.sender === 'agent') {
            last.text = "I was unable to complete this request right now. Please verify your connection or try rephrasing your question.";
            last.isError = true;
            last.streaming = false;
          }
          return updated;
        });
      }
    } finally {
      setLoading(false);
      setCurrentStep('');
    }
  };

  return (
    <Card className="flex flex-col h-[620px] shadow-sm">
      <CardHeader
        title="AgentX Assistant"
        action={
          <div className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-medium">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span>Assistant Ready</span>
          </div>
        }
      />

      {/* Message history */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((m, idx) => (
          <div
            key={idx}
            className={`flex flex-col ${m.sender === 'user' ? 'items-end' : 'items-start'}`}
          >
            <div
              className={`max-w-[90%] rounded-xl p-3.5 text-xs leading-relaxed ${
                m.sender === 'user'
                  ? 'bg-[var(--color-primary)] text-white rounded-br-none'
                  : 'bg-[var(--color-background)] border border-[var(--color-border)] text-[var(--color-foreground)] rounded-bl-none shadow-sm'
              }`}
            >
              {m.agentType && (
                <div className="flex items-center justify-between gap-2 mb-1.5 pb-1 border-b border-[var(--color-border)]/50">
                  <div className="flex items-center gap-1.5 text-[10px] font-bold text-[var(--color-primary)] uppercase tracking-wider">
                    <Bot className="w-3.5 h-3.5" />
                    <span>{m.agentType}</span>
                  </div>
                  {m.latencyMs && (
                    <span className="text-[9px] font-mono text-[var(--color-muted-foreground)]">
                      ⚡ {m.latencyMs}ms
                    </span>
                  )}
                </div>
              )}

              <p className="whitespace-pre-wrap">
                {m.text}
                {m.streaming && (
                  <span className="inline-block w-1.5 h-3.5 bg-[var(--color-primary)] ml-1 animate-pulse align-middle" />
                )}
              </p>

              {/* Action Card: Verified Institutional RAG Citations */}
              {m.actionData && m.actionData.sources && m.actionData.sources.length > 0 && (
                <div className="mt-3 p-2.5 rounded-lg bg-sky-500/10 border border-sky-500/30 text-sky-900 dark:text-sky-200 space-y-1.5">
                  <div className="flex items-center gap-1.5 font-bold text-xs">
                    <BookOpen className="w-3.5 h-3.5 text-sky-600 dark:text-sky-400" />
                    <span>Verified Handbook Citations:</span>
                  </div>
                  {m.actionData.sources.map((src, sIdx) => (
                    <div key={sIdx} className="text-[10px] font-mono bg-[var(--color-card)]/80 p-1.5 rounded border border-sky-500/20">
                      📌 {src}
                    </div>
                  ))}
                </div>
              )}

              {/* Action Card: Grievance Incident */}
              {m.actionData && m.actionData.ticketNumber && (
                <div className="mt-3 p-2.5 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-[var(--color-foreground)] space-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-1.5 font-bold font-mono text-[var(--color-primary)] text-xs">
                      <Wrench className="w-3 h-3 text-amber-500" />
                      <span>Incident #{m.actionData.ticketNumber}</span>
                    </div>
                    <Badge variant="warning" size="sm">Logged</Badge>
                  </div>
                  <p className="text-[10px] text-[var(--color-muted-foreground)]">
                    Location: <strong>{m.actionData.location}</strong> • Facilities Dispatched
                  </p>
                </div>
              )}

              {/* Action Card: Task Planner */}
              {m.actionData && m.actionData.tasksCreated && (
                <div className="mt-3 p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-800 dark:text-emerald-300 space-y-1">
                  <div className="flex items-center gap-1.5 font-bold text-xs">
                    <CheckSquare className="w-3.5 h-3.5" />
                    <span>Tasks Added to Your Dashboard:</span>
                  </div>
                  {m.actionData.tasksCreated.map((t, tIdx) => (
                    <p key={tIdx} className="text-[10px]">✓ {t}</p>
                  ))}
                </div>
              )}

              {/* Deterministic Verification Result */}
              {m.verification && (
                <div className="mt-3 p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-800 dark:text-emerald-300 space-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-1.5 font-bold text-xs">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
                      <span>Verification Check: {m.verification.status}</span>
                    </div>
                    <span className="text-[9px] font-mono text-emerald-700 dark:text-emerald-400">
                      {m.verification.verifiedBy}
                    </span>
                  </div>
                  <p className="text-[10px] text-emerald-900 dark:text-emerald-200">{m.verification.message}</p>
                  {m.verification.checksPerformed && m.verification.checksPerformed.length > 0 && (
                    <div className="pt-1 border-t border-emerald-500/20 text-[9px] font-mono space-y-0.5">
                      {m.verification.checksPerformed.map((c, cIdx) => (
                        <p key={cIdx}>✓ {c}</p>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Orchestrator Execution Plan */}
              {m.executionPlan && m.executionPlan.steps && m.executionPlan.steps.length > 0 && (
                <div className="mt-3 p-2.5 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-[var(--color-foreground)] space-y-1.5">
                  <div className="flex items-center justify-between text-xs font-bold text-[var(--color-primary)]">
                    <div className="flex items-center gap-1.5">
                      <Layers className="w-3.5 h-3.5" />
                      <span>Orchestrator Plan: {m.executionPlan.intent}</span>
                    </div>
                    <Badge variant="primary" size="sm">{m.executionPlan.status}</Badge>
                  </div>
                  <div className="space-y-1 pt-1 font-mono text-[9px]">
                    {m.executionPlan.steps.map((s, sIdx) => (
                      <div key={sIdx} className="flex items-center justify-between p-1 rounded bg-[var(--color-background)] border border-[var(--color-border)]/50">
                        <div className="flex items-center gap-1.5">
                          <span className="text-[var(--color-primary)] font-bold">{s.step}.</span>
                          <span className="font-semibold">{s.agent}</span>
                          <span className="text-[var(--color-muted-foreground)]">({s.tool})</span>
                        </div>
                        <span className="text-emerald-600 dark:text-emerald-400 font-bold">{s.status}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Multi-Agent Execution Steps */}
              {m.steps && m.steps.length > 0 && (
                <div className="mt-2.5 pt-2 border-t border-[var(--color-border)]/60 text-[10px] text-[var(--color-muted-foreground)] space-y-0.5">
                  <div className="flex items-center gap-1 font-semibold text-[9px] uppercase tracking-wider text-[var(--color-primary)] mb-1">
                    <Layers className="w-2.5 h-2.5" />
                    <span>Multi-Agent Execution Pipeline</span>
                  </div>
                  {m.steps.map((s, sIdx) => (
                    <div key={sIdx} className="flex items-center gap-1.5 font-mono">
                      <span className="text-emerald-500">✓</span>
                      <span>{s}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))}

        {loading && !messages[messages.length - 1]?.text && (
          <div className="flex flex-col items-start space-y-1.5 animate-pulse">
            <div className="flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-[var(--color-background)] border border-[var(--color-border)] text-xs text-[var(--color-muted-foreground)]">
              <Loader2 className="w-3.5 h-3.5 animate-spin text-[var(--color-primary)]" />
              <span>{currentStep || "Orchestrating agents..."}</span>
            </div>
          </div>
        )}
      </div>

      {/* Suggested prompts */}
      <div className="px-4 py-2 border-t border-[var(--color-border)] bg-[var(--color-background)]/30 flex items-center gap-2 overflow-x-auto no-scrollbar">
        <span className="text-[10px] text-[var(--color-muted-foreground)] uppercase tracking-wider shrink-0 font-semibold">Try:</span>
        {suggestions.map((s, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(s)}
            className="text-[11px] whitespace-nowrap px-2.5 py-1 rounded-full bg-[var(--color-card)] border border-[var(--color-border)] text-[var(--color-foreground)] hover:border-[var(--color-primary)]/40 hover:bg-[var(--color-primary)]/5 transition-all"
          >
            {s}
          </button>
        ))}
      </div>

      {/* Input bar */}
      <div className="p-3 border-t border-[var(--color-border)] bg-[var(--color-card)]">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="flex items-center gap-2"
        >
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Ask about regulations, attendance condonation, hostel rules, or general questions..."
            className="flex-1 bg-[var(--color-background)] text-[var(--color-foreground)] placeholder-[var(--color-muted-foreground)] text-xs rounded-lg px-3 py-2.5 border border-[var(--color-border)] focus:outline-none focus:border-[var(--color-primary)] transition-colors"
          />
          <Button
            type="submit"
            size="sm"
            disabled={!query.trim() || loading}
            icon={Send}
          >
            Send
          </Button>
        </form>
      </div>
    </Card>
  );
};

export default AgentXAssistant;
